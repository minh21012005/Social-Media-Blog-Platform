package com.socialmediablog.platform.services.interaction.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.socialmediablog.platform.services.interaction.domain.aggregate.Interaction;
import com.socialmediablog.platform.services.interaction.domain.model.InteractionTargetType;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractorId;
import com.socialmediablog.platform.services.interaction.domain.vo.TargetId;
import com.socialmediablog.platform.services.interaction.infrastructure.entity.JpaInteractionEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:interaction_persistence;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InteractionPersistenceTests {

    private static final Instant NOW = Instant.parse("2026-05-28T00:00:00Z");

    @Autowired
    private SpringDataJpaInteractionRepository repository;

    @Test
    void mapsInteractionDomainAndJpaBothWays() {
        Interaction interaction = interaction(UUID.randomUUID(), UUID.randomUUID());

        Interaction mapped = JpaInteractionEntity.fromDomain(interaction).toDomain();

        assertThat(mapped.userId()).isEqualTo(interaction.userId());
        assertThat(mapped.targetType()).isEqualTo(InteractionTargetType.ARTICLE);
        assertThat(mapped.targetId()).isEqualTo(interaction.targetId());
        assertThat(mapped.clapCount()).isEqualTo(3);
    }

    @Test
    void rejectsDuplicateUserTargetInteraction() {
        UUID userId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        repository.saveAndFlush(JpaInteractionEntity.fromDomain(interaction(userId, targetId)));

        assertThatThrownBy(() -> repository.saveAndFlush(JpaInteractionEntity.fromDomain(interaction(userId, targetId))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsInvalidClapCountInDomain() {
        assertThatThrownBy(() -> Interaction.record(
                InteractorId.of(UUID.randomUUID()),
                InteractionTargetType.ARTICLE,
                TargetId.of(UUID.randomUUID()),
                0,
                NOW
        )).isInstanceOf(IllegalArgumentException.class);
    }

    private static Interaction interaction(UUID userId, UUID targetId) {
        return Interaction.record(
                InteractorId.of(userId),
                InteractionTargetType.ARTICLE,
                TargetId.of(targetId),
                3,
                NOW
        );
    }
}
