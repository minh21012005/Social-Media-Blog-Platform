package com.socialmediablog.platform.services.interaction.infrastructure.adapter;

import com.socialmediablog.platform.services.interaction.domain.aggregate.Interaction;
import com.socialmediablog.platform.services.interaction.domain.model.InteractionTargetType;
import com.socialmediablog.platform.services.interaction.domain.repository.InteractionRepository;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractorId;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractionId;
import com.socialmediablog.platform.services.interaction.domain.vo.TargetId;
import com.socialmediablog.platform.services.interaction.infrastructure.entity.JpaInteractionEntity;
import com.socialmediablog.platform.services.interaction.infrastructure.persistence.SpringDataJpaInteractionRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaInteractionRepositoryAdapter implements InteractionRepository {

    private final SpringDataJpaInteractionRepository repository;

    public JpaInteractionRepositoryAdapter(SpringDataJpaInteractionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Interaction> findById(InteractionId id) {
        return repository.findById(id.value()).map(JpaInteractionEntity::toDomain);
    }

    @Override
    public Optional<Interaction> findByUserIdAndTarget(
            InteractorId userId,
            InteractionTargetType targetType,
            TargetId targetId
    ) {
        return repository.findByUserIdAndTargetTypeAndTargetId(userId.value(), targetType.name(), targetId.value())
                .map(JpaInteractionEntity::toDomain);
    }

    @Override
    public boolean existsByUserIdAndTarget(InteractorId userId, InteractionTargetType targetType, TargetId targetId) {
        return repository.existsByUserIdAndTargetTypeAndTargetId(userId.value(), targetType.name(), targetId.value());
    }

    @Override
    public long totalClapsByTarget(InteractionTargetType targetType, TargetId targetId) {
        return repository.sumClapCountByTargetTypeAndTargetId(targetType.name(), targetId.value());
    }

    @Override
    public Interaction save(Interaction interaction) {
        return repository.save(JpaInteractionEntity.fromDomain(interaction)).toDomain();
    }
}
