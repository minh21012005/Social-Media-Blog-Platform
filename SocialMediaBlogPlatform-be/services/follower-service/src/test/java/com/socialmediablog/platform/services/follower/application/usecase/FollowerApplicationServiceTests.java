package com.socialmediablog.platform.services.follower.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.socialmediablog.platform.services.follower.application.command.FollowUserCommand;
import com.socialmediablog.platform.services.follower.application.command.GetFollowCountsCommand;
import com.socialmediablog.platform.services.follower.application.command.GetFollowStatusCommand;
import com.socialmediablog.platform.services.follower.application.command.ListFollowersCommand;
import com.socialmediablog.platform.services.follower.application.command.ListFollowingCommand;
import com.socialmediablog.platform.services.follower.application.command.UnfollowUserCommand;
import com.socialmediablog.platform.services.follower.application.result.FollowCounts;
import com.socialmediablog.platform.services.follower.application.result.FollowRelationView;
import com.socialmediablog.platform.services.follower.application.result.FollowStatus;
import com.socialmediablog.platform.services.follower.application.result.FollowUserPage;
import com.socialmediablog.platform.services.follower.domain.aggregate.FollowRelation;
import com.socialmediablog.platform.services.follower.domain.repository.FollowRelationRepository;
import com.socialmediablog.platform.services.follower.domain.vo.FollowedUserId;
import com.socialmediablog.platform.services.follower.domain.vo.FollowRelationId;
import com.socialmediablog.platform.services.follower.domain.vo.FollowerId;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FollowerApplicationServiceTests {

    private InMemoryFollowRelationRepository repository;
    private FollowerApplicationService service;

    @BeforeEach
    void setUp() {
        repository = new InMemoryFollowRelationRepository();
        service = new FollowerApplicationService(repository);
    }

    @Test
    void followCreatesActiveRelationAndIsIdempotent() {
        UUID followerId = UUID.randomUUID();
        UUID followedUserId = UUID.randomUUID();

        FollowRelationView created = service.execute(new FollowUserCommand(followerId, followedUserId));
        FollowRelationView repeated = service.execute(new FollowUserCommand(followerId, followedUserId));

        assertThat(created.following()).isTrue();
        assertThat(repeated.following()).isTrue();
        assertThat(repeated.id()).isEqualTo(created.id());
        assertThat(repository.countActiveFollowers(FollowedUserId.of(followedUserId))).isEqualTo(1);
    }

    @Test
    void unfollowSoftDisablesExistingRelationAndCanReactivate() {
        UUID followerId = UUID.randomUUID();
        UUID followedUserId = UUID.randomUUID();
        FollowRelationView followed = service.execute(new FollowUserCommand(followerId, followedUserId));

        FollowRelationView unfollowed = service.execute(new UnfollowUserCommand(followerId, followedUserId));
        FollowRelationView reactivated = service.execute(new FollowUserCommand(followerId, followedUserId));

        assertThat(unfollowed.id()).isEqualTo(followed.id());
        assertThat(unfollowed.following()).isFalse();
        assertThat(unfollowed.unfollowedAt()).isNotNull();
        assertThat(reactivated.id()).isEqualTo(followed.id());
        assertThat(reactivated.following()).isTrue();
        assertThat(reactivated.unfollowedAt()).isNull();
    }

    @Test
    void unfollowMissingRelationIsNoOp() {
        UUID followerId = UUID.randomUUID();
        UUID followedUserId = UUID.randomUUID();

        FollowRelationView result = service.execute(new UnfollowUserCommand(followerId, followedUserId));

        assertThat(result.id()).isNull();
        assertThat(result.following()).isFalse();
        assertThat(repository.countActiveFollowers(FollowedUserId.of(followedUserId))).isZero();
    }

    @Test
    void statusAndCountsOnlyIncludeActiveRelations() {
        UUID targetUserId = UUID.randomUUID();
        UUID activeFollowerId = UUID.randomUUID();
        UUID unfollowedUserId = UUID.randomUUID();

        service.execute(new FollowUserCommand(activeFollowerId, targetUserId));
        service.execute(new FollowUserCommand(unfollowedUserId, targetUserId));
        service.execute(new UnfollowUserCommand(unfollowedUserId, targetUserId));

        FollowStatus activeStatus = service.execute(new GetFollowStatusCommand(activeFollowerId, targetUserId));
        FollowStatus inactiveStatus = service.execute(new GetFollowStatusCommand(unfollowedUserId, targetUserId));
        FollowCounts counts = service.execute(new GetFollowCountsCommand(targetUserId));

        assertThat(activeStatus.following()).isTrue();
        assertThat(inactiveStatus.following()).isFalse();
        assertThat(counts.followers()).isEqualTo(1);
        assertThat(counts.following()).isZero();
    }

    @Test
    void listsFollowersAndFollowing() {
        UUID userId = UUID.randomUUID();
        UUID followerId = UUID.randomUUID();
        UUID followedUserId = UUID.randomUUID();
        service.execute(new FollowUserCommand(followerId, userId));
        service.execute(new FollowUserCommand(userId, followedUserId));

        FollowUserPage followers = service.execute(new ListFollowersCommand(userId, 0, 20));
        FollowUserPage following = service.execute(new ListFollowingCommand(userId, 0, 20));

        assertThat(followers.users()).extracting("userId").containsExactly(followerId);
        assertThat(followers.total()).isEqualTo(1);
        assertThat(following.users()).extracting("userId").containsExactly(followedUserId);
        assertThat(following.total()).isEqualTo(1);
    }

    @Test
    void rejectsSelfFollow() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> service.execute(new FollowUserCommand(userId, userId)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static class InMemoryFollowRelationRepository implements FollowRelationRepository {

        private final Map<UUID, FollowRelation> relations = new LinkedHashMap<>();

        @Override
        public Optional<FollowRelation> findById(FollowRelationId id) {
            return Optional.ofNullable(relations.get(id.value()));
        }

        @Override
        public Optional<FollowRelation> findByFollowerIdAndFollowedUserId(
                FollowerId followerId,
                FollowedUserId followedUserId
        ) {
            return relations.values().stream()
                    .filter(relation -> relation.followerId().equals(followerId))
                    .filter(relation -> relation.followedUserId().equals(followedUserId))
                    .findFirst();
        }

        @Override
        public List<FollowRelation> findActiveFollowers(FollowedUserId followedUserId, int page, int size) {
            return activeRelations().stream()
                    .filter(relation -> relation.followedUserId().equals(followedUserId))
                    .skip((long) page * size)
                    .limit(size)
                    .toList();
        }

        @Override
        public List<FollowRelation> findActiveFollowing(FollowerId followerId, int page, int size) {
            return activeRelations().stream()
                    .filter(relation -> relation.followerId().equals(followerId))
                    .skip((long) page * size)
                    .limit(size)
                    .toList();
        }

        @Override
        public long countActiveFollowers(FollowedUserId followedUserId) {
            return activeRelations().stream()
                    .filter(relation -> relation.followedUserId().equals(followedUserId))
                    .count();
        }

        @Override
        public long countActiveFollowing(FollowerId followerId) {
            return activeRelations().stream()
                    .filter(relation -> relation.followerId().equals(followerId))
                    .count();
        }

        @Override
        public FollowRelation save(FollowRelation followRelation) {
            relations.put(followRelation.id().value(), followRelation);
            return followRelation;
        }

        private List<FollowRelation> activeRelations() {
            return relations.values().stream()
                    .filter(FollowRelation::isActive)
                    .sorted(Comparator.comparing(FollowRelation::followedAt).reversed())
                    .toList();
        }
    }
}
