package com.socialmediablog.platform.services.follower.application.usecase;

import com.socialmediablog.platform.services.follower.application.command.FollowUserCommand;
import com.socialmediablog.platform.services.follower.application.command.GetFollowCountsCommand;
import com.socialmediablog.platform.services.follower.application.command.GetFollowStatusCommand;
import com.socialmediablog.platform.services.follower.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.follower.application.command.ListFollowersCommand;
import com.socialmediablog.platform.services.follower.application.command.ListFollowingCommand;
import com.socialmediablog.platform.services.follower.application.command.UnfollowUserCommand;
import com.socialmediablog.platform.services.follower.application.port.in.FollowUserUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.GetFollowCountsUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.GetFollowStatusUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.ListFollowersUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.ListFollowingUseCase;
import com.socialmediablog.platform.services.follower.application.port.in.UnfollowUserUseCase;
import com.socialmediablog.platform.services.follower.application.port.out.FollowerEventPublisher;
import com.socialmediablog.platform.services.follower.application.result.FollowCounts;
import com.socialmediablog.platform.services.follower.application.result.FollowRelationView;
import com.socialmediablog.platform.services.follower.application.result.FollowStatus;
import com.socialmediablog.platform.services.follower.application.result.FollowUserItem;
import com.socialmediablog.platform.services.follower.application.result.FollowUserPage;
import com.socialmediablog.platform.services.follower.application.result.ServiceStatus;
import com.socialmediablog.platform.services.follower.domain.aggregate.FollowRelation;
import com.socialmediablog.platform.services.follower.domain.event.UserFollowedEvent;
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
        GetFollowCountsUseCase {

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
                .orElseGet(() -> new FollowRelationView(null, command.followerId(), command.followedUserId(), false, null, null));
    }

    @Override
    @Transactional(readOnly = true)
    public FollowStatus execute(GetFollowStatusCommand command) {
        FollowerId viewerId = FollowerId.of(command.viewerId());
        FollowedUserId targetUserId = FollowedUserId.of(command.targetUserId());
        boolean following = followRelationRepository.findByFollowerIdAndFollowedUserId(viewerId, targetUserId)
                .map(FollowRelation::isActive)
                .orElse(false);
        return new FollowStatus(command.viewerId(), command.targetUserId(), following);
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
