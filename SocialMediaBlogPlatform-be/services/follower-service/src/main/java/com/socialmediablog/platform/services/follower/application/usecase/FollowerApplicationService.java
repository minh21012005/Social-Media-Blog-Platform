package com.socialmediablog.platform.services.follower.application.usecase;

import com.socialmediablog.platform.services.follower.application.command.BlockUserCommand;
import com.socialmediablog.platform.services.follower.application.command.CheckMutualFollowCommand;
import com.socialmediablog.platform.services.follower.application.command.FollowUserCommand;
import com.socialmediablog.platform.services.follower.application.command.GetBlockStatusCommand;
import com.socialmediablog.platform.services.follower.application.command.GetFollowCountsCommand;
import com.socialmediablog.platform.services.follower.application.command.GetFollowStatusCommand;
import com.socialmediablog.platform.services.follower.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.follower.application.command.ListBlockedUsersCommand;
import com.socialmediablog.platform.services.follower.application.command.ListFollowersCommand;
import com.socialmediablog.platform.services.follower.application.command.ListFollowingCommand;
import com.socialmediablog.platform.services.follower.application.command.UnblockUserCommand;
import com.socialmediablog.platform.services.follower.application.command.UnfollowUserCommand;
import com.socialmediablog.platform.services.follower.application.port.in.BlockUserUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.CheckMutualFollowUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.FollowUserUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.GetBlockStatusUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.GetFollowCountsUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.GetFollowStatusUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.ListBlockedUsersUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.ListFollowersUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.ListFollowingUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.UnblockUserUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.UnfollowUserUseCase;
import com.socialmediablog.platform.services.follower.application.port.out.FollowerEventPublisher;
import com.socialmediablog.platform.services.follower.application.result.BlockStatus;
import com.socialmediablog.platform.services.follower.application.result.FollowCounts;
import com.socialmediablog.platform.services.follower.application.result.FollowRelationView;
import com.socialmediablog.platform.services.follower.application.result.FollowStatus;
import com.socialmediablog.platform.services.follower.application.result.FollowUserItem;
import com.socialmediablog.platform.services.follower.application.result.FollowUserPage;
import com.socialmediablog.platform.services.follower.application.result.MutualFollowStatus;
import com.socialmediablog.platform.services.follower.application.result.ServiceStatus;
import com.socialmediablog.platform.services.follower.domain.aggregate.FollowRelation;
import com.socialmediablog.platform.services.follower.domain.event.UserBlockedEvent;
import com.socialmediablog.platform.services.follower.domain.event.UserFollowedEvent;
import com.socialmediablog.platform.services.follower.domain.event.UserUnblockedEvent;
import com.socialmediablog.platform.services.follower.domain.event.UserUnfollowedEvent;
import com.socialmediablog.platform.services.follower.domain.repository.FollowRelationRepository;
import com.socialmediablog.platform.services.follower.domain.vo.FollowedUserId;
import com.socialmediablog.platform.services.follower.domain.vo.FollowerId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowerApplicationService implements
        GetServiceStatusUseCase,
        FollowUserUseCase,
        UnfollowUserUseCase,
        GetFollowStatusUseCase,
        ListFollowersUseCase,
        ListFollowingUseCase,
        GetFollowCountsUseCase,
        BlockUserUseCase,
        UnblockUserUseCase,
        GetBlockStatusUseCase,
        ListBlockedUsersUseCase,
        CheckMutualFollowUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final FollowRelationRepository followRelationRepository;
    private final FollowerEventPublisher followerEventPublisher;

    public FollowerApplicationService(
            FollowRelationRepository followRelationRepository,
            FollowerEventPublisher followerEventPublisher) {
        this.followRelationRepository = followRelationRepository;
        this.followerEventPublisher = followerEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceStatus execute(GetServiceStatusCommand command) {
        return new ServiceStatus("follower-service", "followers", command.currentUserId());
    }

    @Override
    @Transactional
    public FollowRelationView execute(FollowUserCommand command) {
        FollowerId followerId = FollowerId.of(command.followerId());
        FollowedUserId followedUserId = FollowedUserId.of(command.followedUserId());
        Instant now = Instant.now();

        // Guard: check if the target has blocked the follower
        followRelationRepository.findByFollowerIdAndFollowedUserId(
                FollowerId.of(command.followedUserId()), FollowedUserId.of(command.followerId())
        ).ifPresent(reverse -> {
            if (reverse.isBlocked()) {
                throw new IllegalArgumentException("Cannot follow this user");
            }
        });

        // Guard: check if the follower has blocked the target
        followRelationRepository.findByFollowerIdAndFollowedUserId(followerId, followedUserId)
                .ifPresent(existing -> {
                    if (existing.isBlocked()) {
                        throw new IllegalArgumentException("Unblock this user before following");
                    }
                });

        FollowRelation relation = followRelationRepository.findByFollowerIdAndFollowedUserId(followerId, followedUserId)
                .map(existing -> existing.activate(now))
                .orElseGet(() -> FollowRelation.follow(followerId, followedUserId, now));

        FollowRelation saved = followRelationRepository.save(relation);
        followerEventPublisher.publish(
                saved.id().value(),
                new UserFollowedEvent(UUID.randomUUID(), command.followerId(), command.followedUserId(), now)
        );
        return FollowRelationView.from(saved);
    }

    @Override
    @Transactional
    public FollowRelationView execute(UnfollowUserCommand command) {
        FollowerId followerId = FollowerId.of(command.followerId());
        FollowedUserId followedUserId = FollowedUserId.of(command.followedUserId());
        Instant now = Instant.now();

        return followRelationRepository.findByFollowerIdAndFollowedUserId(followerId, followedUserId)
                .map(existing -> existing.unfollow(now))
                .map(followRelationRepository::save)
                .map(saved -> {
                    followerEventPublisher.publish(
                            saved.id().value(),
                            new UserUnfollowedEvent(UUID.randomUUID(), command.followerId(), command.followedUserId(), now)
                    );
                    return FollowRelationView.from(saved);
                })
                .orElseGet(() -> new FollowRelationView(null, command.followerId(), command.followedUserId(), false, false, null, null));
    }

    @Override
    @Transactional(readOnly = true)
    public FollowStatus execute(GetFollowStatusCommand command) {
        FollowerId viewerId = FollowerId.of(command.viewerId());
        FollowedUserId targetUserId = FollowedUserId.of(command.targetUserId());

        boolean following = followRelationRepository.findByFollowerIdAndFollowedUserId(viewerId, targetUserId)
                .map(FollowRelation::isActive)
                .orElse(false);

        boolean blocked = followRelationRepository.findByFollowerIdAndFollowedUserId(viewerId, targetUserId)
                .map(FollowRelation::isBlocked)
                .orElse(false);

        boolean mutualFollow = false;
        if (following) {
            mutualFollow = followRelationRepository.findByFollowerIdAndFollowedUserId(
                    FollowerId.of(command.targetUserId()), FollowedUserId.of(command.viewerId())
            ).map(FollowRelation::isActive).orElse(false);
        }

        return new FollowStatus(command.viewerId(), command.targetUserId(), following, blocked, mutualFollow);
    }

    @Override
    @Transactional(readOnly = true)
    public FollowUserPage execute(ListFollowersCommand command) {
        UUID userId = requireUserId(command.userId());
        int page = normalizePage(command.page());
        int size = normalizeSize(command.size());
        FollowedUserId followedUserId = FollowedUserId.of(userId);
        List<FollowUserItem> followers = followRelationRepository.findActiveFollowers(followedUserId, page, size).stream()
                .map(relation -> new FollowUserItem(relation.followerId().value(), relation.followedAt()))
                .toList();
        return new FollowUserPage(
                userId,
                followers,
                page,
                size,
                followRelationRepository.countActiveFollowers(followedUserId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public FollowUserPage execute(ListFollowingCommand command) {
        UUID userId = requireUserId(command.userId());
        int page = normalizePage(command.page());
        int size = normalizeSize(command.size());
        FollowerId followerId = FollowerId.of(userId);
        List<FollowUserItem> following = followRelationRepository.findActiveFollowing(followerId, page, size).stream()
                .map(relation -> new FollowUserItem(relation.followedUserId().value(), relation.followedAt()))
                .toList();
        return new FollowUserPage(
                userId,
                following,
                page,
                size,
                followRelationRepository.countActiveFollowing(followerId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public FollowCounts execute(GetFollowCountsCommand command) {
        UUID userId = requireUserId(command.userId());
        return new FollowCounts(
                userId,
                followRelationRepository.countActiveFollowers(FollowedUserId.of(userId)),
                followRelationRepository.countActiveFollowing(FollowerId.of(userId))
        );
    }

    @Override
    @Transactional
    public FollowRelationView execute(BlockUserCommand command) {
        FollowerId blockerId = FollowerId.of(command.blockerId());
        FollowedUserId blockedUserId = FollowedUserId.of(command.blockedUserId());
        Instant now = Instant.now();

        // Block the relationship (blocker -> blocked)
        FollowRelation relation = followRelationRepository.findByFollowerIdAndFollowedUserId(blockerId, blockedUserId)
                .map(existing -> existing.block(now))
                .orElseGet(() -> FollowRelation.blockNew(blockerId, blockedUserId, now));
        FollowRelation saved = followRelationRepository.save(relation);

        // Auto-unfollow the reverse direction (blocked -> blocker) if active
        followRelationRepository.findByFollowerIdAndFollowedUserId(
                FollowerId.of(command.blockedUserId()), FollowedUserId.of(command.blockerId())
        ).ifPresent(reverse -> {
            if (reverse.isActive()) {
                followRelationRepository.save(reverse.unfollow(now));
            }
        });

        followerEventPublisher.publish(
                saved.id().value(),
                new UserBlockedEvent(UUID.randomUUID(), command.blockerId(), command.blockedUserId(), now)
        );
        return FollowRelationView.from(saved);
    }

    @Override
    @Transactional
    public FollowRelationView execute(UnblockUserCommand command) {
        FollowerId blockerId = FollowerId.of(command.blockerId());
        FollowedUserId blockedUserId = FollowedUserId.of(command.blockedUserId());
        Instant now = Instant.now();

        return followRelationRepository.findByFollowerIdAndFollowedUserId(blockerId, blockedUserId)
                .filter(FollowRelation::isBlocked)
                .map(existing -> existing.unblock(now))
                .map(followRelationRepository::save)
                .map(saved -> {
                    followerEventPublisher.publish(
                            saved.id().value(),
                            new UserUnblockedEvent(UUID.randomUUID(), command.blockerId(), command.blockedUserId(), now)
                    );
                    return FollowRelationView.from(saved);
                })
                .orElseGet(() -> new FollowRelationView(null, command.blockerId(), command.blockedUserId(), false, false, null, null));
    }

    @Override
    @Transactional(readOnly = true)
    public BlockStatus execute(GetBlockStatusCommand command) {
        boolean blocked = followRelationRepository.findByFollowerIdAndFollowedUserId(
                FollowerId.of(command.viewerId()), FollowedUserId.of(command.targetUserId())
        ).map(FollowRelation::isBlocked).orElse(false);
        return new BlockStatus(command.viewerId(), command.targetUserId(), blocked);
    }

    @Override
    @Transactional(readOnly = true)
    public FollowUserPage execute(ListBlockedUsersCommand command) {
        UUID userId = requireUserId(command.userId());
        int page = normalizePage(command.page());
        int size = normalizeSize(command.size());
        FollowerId blockerId = FollowerId.of(userId);
        List<FollowUserItem> blocked = followRelationRepository.findBlockedByUser(blockerId, page, size).stream()
                .map(relation -> new FollowUserItem(relation.followedUserId().value(), relation.updatedAt()))
                .toList();
        return new FollowUserPage(
                userId,
                blocked,
                page,
                size,
                followRelationRepository.countBlockedByUser(blockerId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MutualFollowStatus execute(CheckMutualFollowCommand command) {
        boolean aFollowsB = followRelationRepository.findByFollowerIdAndFollowedUserId(
                FollowerId.of(command.userIdA()), FollowedUserId.of(command.userIdB())
        ).map(FollowRelation::isActive).orElse(false);

        boolean bFollowsA = followRelationRepository.findByFollowerIdAndFollowedUserId(
                FollowerId.of(command.userIdB()), FollowedUserId.of(command.userIdA())
        ).map(FollowRelation::isActive).orElse(false);

        return new MutualFollowStatus(command.userIdA(), command.userIdB(), aFollowsB && bFollowsA);
    }

    private static UUID requireUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id is required");
        }
        return userId;
    }

    private static int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private static int normalizeSize(int size) {
        if (size < 1) {
            return 20;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
