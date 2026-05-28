package com.socialmediablog.platform.services.follower.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.socialmediablog.platform.services.follower.domain.aggregate.FollowRelation;
import com.socialmediablog.platform.services.follower.domain.vo.FollowedUserId;
import com.socialmediablog.platform.services.follower.domain.vo.FollowerId;
import com.socialmediablog.platform.services.follower.infrastructure.entity.JpaFollowRelationEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:follower_persistence;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FollowRelationPersistenceTests {

    private static final Instant NOW = Instant.parse("2026-05-28T00:00:00Z");

    @Autowired
    private SpringDataJpaFollowRelationRepository repository;

    @Test
    void mapsFollowRelationDomainAndJpaBothWays() {
        FollowRelation relation = followRelation(UUID.randomUUID(), UUID.randomUUID());

        FollowRelation mapped = JpaFollowRelationEntity.fromDomain(relation).toDomain();

        assertThat(mapped.followerId()).isEqualTo(relation.followerId());
        assertThat(mapped.followedUserId()).isEqualTo(relation.followedUserId());
        assertThat(mapped.status()).isEqualTo(relation.status());
        assertThat(mapped.unfollowedAt()).isNull();
    }

    @Test
    void rejectsDuplicateFollowPair() {
        UUID followerId = UUID.randomUUID();
        UUID followedUserId = UUID.randomUUID();
        repository.saveAndFlush(JpaFollowRelationEntity.fromDomain(followRelation(followerId, followedUserId)));

        assertThatThrownBy(() -> repository.saveAndFlush(JpaFollowRelationEntity.fromDomain(followRelation(followerId, followedUserId))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsSelfFollowInDomain() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> FollowRelation.follow(
                FollowerId.of(userId),
                FollowedUserId.of(userId),
                NOW
        )).isInstanceOf(IllegalArgumentException.class);
    }

    private static FollowRelation followRelation(UUID followerId, UUID followedUserId) {
        return FollowRelation.follow(
                FollowerId.of(followerId),
                FollowedUserId.of(followedUserId),
                NOW
        );
    }
}
