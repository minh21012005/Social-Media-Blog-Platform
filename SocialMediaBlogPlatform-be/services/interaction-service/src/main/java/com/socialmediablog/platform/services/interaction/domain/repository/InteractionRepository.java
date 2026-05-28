package com.socialmediablog.platform.services.interaction.domain.repository;

import com.socialmediablog.platform.services.interaction.domain.aggregate.Interaction;
import com.socialmediablog.platform.services.interaction.domain.model.InteractionTargetType;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractorId;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractionId;
import com.socialmediablog.platform.services.interaction.domain.vo.TargetId;
import java.util.Optional;

public interface InteractionRepository {

    Optional<Interaction> findById(InteractionId id);

    Optional<Interaction> findByUserIdAndTarget(InteractorId userId, InteractionTargetType targetType, TargetId targetId);

    Interaction save(Interaction interaction);
}
