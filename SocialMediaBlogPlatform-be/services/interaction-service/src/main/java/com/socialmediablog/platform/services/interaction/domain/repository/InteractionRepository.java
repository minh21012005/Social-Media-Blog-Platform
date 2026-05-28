package com.socialmediablog.platform.services.interaction.domain.repository;

import com.socialmediablog.platform.services.interaction.domain.aggregate.Interaction;
import com.socialmediablog.platform.services.interaction.domain.vo.InteractionId;
import java.util.Optional;

public interface InteractionRepository {

    Optional<Interaction> findById(InteractionId id);

    Interaction save(Interaction interaction);
}
