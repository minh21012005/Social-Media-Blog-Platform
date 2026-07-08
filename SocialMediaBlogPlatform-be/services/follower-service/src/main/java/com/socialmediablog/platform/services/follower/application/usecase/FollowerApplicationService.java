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
import com.socialmediablog.platform.services.follower.domain.repository.MuteRepository;
import com.socialmediablog.platform.services.follower.domain.aggregate.Mute;
import com.socialmediablog.platform.services.follower.infrastructure.feign.UserServiceFeignClient;
import com.socialmediablog.platform.services.follower.application.port.in.AcceptFollowRequestUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.RejectFollowRequestUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.ListPendingFollowRequestsUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.MuteUserUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.UnmuteUserUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.GetMuteStatusUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.ListMutedUsersUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.ListMutedUserIdsUseCase;
import com.socialmediablog.platform.services.follower.domain.event.UserFollowRequestedEvent;
import com.socialmediablog.platform.services.follower.domain.event.UserFollowAcceptedEvent;
import com.socialmediablog.platform.services.follower.domain.vo.FollowedUserId;
import com.socialmediablog.platform.services.follower.domain.vo.FollowerId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        CheckMutualFollowUseCase,
        AcceptFollowRequestUseCase,
        RejectFollowRequestUseCase,
        ListPendingFollowRequestsUseCase,
        MuteUserUseCase,
        UnmuteUserUseCase,
        GetMuteStatusUseCase,
        ListMutedUsersUseCase,
        ListMutedUserIdsUseCase {

    private static final Logger log = LoggerFactory.getLogger(FollowerApplicationService.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final FollowRelationRepository followRelationRepository;
    private final FollowerEventPublisher followerEventPublisher;
    private final UserServiceFeignClient userServiceFeignClient;
    private final MuteRepository muteRepository;

    public FollowerApplicationService(
            FollowRelationRepository followRelationRepository,
            FollowerEventPublisher followerEventPublisher,
            UserServiceFeignClient userServiceFeignClient,
            MuteRepository muteRepository) {
        this.followRelationRepository = followRelationRepository;
        this.followerEventPublisher = followerEventPublisher;
        this.userServiceFeignClient = userServiceFeignClient;
        this.muteRepository = muteRepository;
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

        // Fetch isPrivate settings via Feign
        boolean isPrivate = false;
        try {
            var userResponse = userServiceFeignClient.getPublicUser(command.followedUserId());
            if (userResponse != null && userResponse.data() != null) {
                isPrivate = userResponse.data().isPrivate();
            }
        } catch (Exception e) {
            log.error("Failed to fetch user privacy settings via Feign client: {}", e.getMessage());
        }

        final boolean finalIsPrivate = isPrivate;
        FollowRelation relation = followRelationRepository.findByFollowerIdAndFollowedUserId(followerId, followedUserId)
                .map(existing -> {
                    if (finalIsPrivate) {
                        return existing.requestFollow(now);
                    } else {
                        return existing.activate(now);
                    }
                })
                .orElseGet(() -> {
                    if (finalIsPrivate) {
                        return FollowRelation.pendingFollow(followerId, followedUserId, now);
                    } else {
                        return FollowRelation.follow(followerId, followedUserId, now);
                    }
                });

        FollowRelation saved = followRelationRepository.save(relation);
        if (saved.isPending()) {
            followerEventPublisher.publish(
                    saved.id().value(),
                    new UserFollowRequestedEvent(UUID.randomUUID(), command.followerId(), command.followedUserId(), now)
            );
        } else {
            followerEventPublisher.publish(
                    saved.id().value(),
                    new UserFollowedEvent(UUID.randomUUID(), command.followerId(), command.followedUserId(), now)
            );
        }
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
                .orElseGet(() -> new FollowRelationView(null, command.followerId(), command.followedUserId(), false, false, false, null, null));
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

        boolean pending = followRelationRepository.findByFollowerIdAndFollowedUserId(viewerId, targetUserId)
                .map(FollowRelation::isPending)
                .orElse(false);

        boolean mutualFollow = false;
        if (following) {
            mutualFollow = followRelationRepository.findByFollowerIdAndFollowedUserId(
                    FollowerId.of(command.targetUserId()), FollowedUserId.of(command.viewerId())
            ).map(FollowRelation::isActive).orElse(false);
        }

        return new FollowStatus(command.viewerId(), command.targetUserId(), following, blocked, mutualFollow, pending);
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
                .orElseGet(() -> new FollowRelationView(null, command.blockerId(), command.blockedUserId(), false, false, false, null, null));
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

    // --- AcceptFollowRequestUseCase ---
    @Override
    @Transactional
    public FollowRelationView accept(UUID userId, UUID followerId) {
        FollowRelation relation = followRelationRepository.findByFollowerIdAndFollowedUserId(
                FollowerId.of(followerId), FollowedUserId.of(userId)
        ).orElseThrow(() -> new IllegalArgumentException("Follow request not found"));

        if (!relation.isPending()) {
            throw new IllegalStateException("Follow request is not in PENDING state");
        }

        Instant now = Instant.now();
        FollowRelation accepted = relation.accept(now);
        FollowRelation saved = followRelationRepository.save(accepted);

        // Notify B (user who is being followed) -> publish UserFollowedEvent
        followerEventPublisher.publish(
                saved.id().value(),
                new UserFollowedEvent(UUID.randomUUID(), followerId, userId, now)
        );

        // Notify A (follower who sent the request) -> publish UserFollowAcceptedEvent
        followerEventPublisher.publish(
                saved.id().value(),
                new UserFollowAcceptedEvent(UUID.randomUUID(), followerId, userId, now)
        );

        return FollowRelationView.from(saved);
    }

    // --- RejectFollowRequestUseCase ---
    @Override
    @Transactional
    public FollowRelationView reject(UUID userId, UUID followerId) {
        FollowRelation relation = followRelationRepository.findByFollowerIdAndFollowedUserId(
                FollowerId.of(followerId), FollowedUserId.of(userId)
        ).orElseThrow(() -> new IllegalArgumentException("Follow request not found"));

        if (!relation.isPending()) {
            throw new IllegalStateException("Follow request is not in PENDING state");
        }

        Instant now = Instant.now();
        FollowRelation rejected = relation.reject(now);
        FollowRelation saved = followRelationRepository.save(rejected);

        return FollowRelationView.from(saved);
    }

    // --- ListPendingFollowRequestsUseCase ---
    @Override
    @Transactional(readOnly = true)
    public FollowUserPage listPending(UUID userId, int page, int size) {
        UUID checkedUserId = requireUserId(userId);
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        FollowedUserId followedUserId = FollowedUserId.of(checkedUserId);

        List<FollowUserItem> pending = followRelationRepository.findPendingFollowRequests(followedUserId, normalizedPage, normalizedSize).stream()
                .map(relation -> new FollowUserItem(relation.followerId().value(), relation.updatedAt()))
                .toList();

        return new FollowUserPage(
                checkedUserId,
                pending,
                normalizedPage,
                normalizedSize,
                followRelationRepository.countPendingFollowRequests(followedUserId)
        );
    }

    // --- MuteUserUseCase ---
    @Override
    @Transactional
    public void mute(UUID muterId, UUID mutedUserId) {
        if (muterId.equals(mutedUserId)) {
            throw new IllegalArgumentException("Cannot mute yourself");
        }
        Instant now = Instant.now();
        muteRepository.findByMuterIdAndMutedUserId(muterId, mutedUserId)
                .orElseGet(() -> muteRepository.save(Mute.mute(muterId, mutedUserId, now)));
    }

    // --- UnmuteUserUseCase ---
    @Override
    @Transactional
    public void unmute(UUID muterId, UUID mutedUserId) {
        muteRepository.findByMuterIdAndMutedUserId(muterId, mutedUserId)
                .ifPresent(muteRepository::delete);
    }

    // --- GetMuteStatusUseCase ---
    @Override
    @Transactional(readOnly = true)
    public boolean isMuted(UUID muterId, UUID mutedUserId) {
        return muteRepository.findByMuterIdAndMutedUserId(muterId, mutedUserId).isPresent();
    }

    // --- ListMutedUsersUseCase ---
    @Override
    @Transactional(readOnly = true)
    public FollowUserPage listMuted(UUID muterId, int page, int size) {
        UUID checkedMuterId = requireUserId(muterId);
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);

        List<FollowUserItem> mutedList = muteRepository.findByMuterId(checkedMuterId, normalizedPage, normalizedSize).stream()
                .map(mute -> new FollowUserItem(mute.mutedUserId(), mute.createdAt()))
                .toList();

        return new FollowUserPage(
                checkedMuterId,
                mutedList,
                normalizedPage,
                normalizedSize,
                muteRepository.countByMuterId(checkedMuterId)
        );
    }

    // --- ListMutedUserIdsUseCase ---
    @Override
    @Transactional(readOnly = true)
    public List<UUID> listMutedIds(UUID muterId) {
        return muteRepository.findMutedUserIdsByMuterId(muterId);
    }
}
