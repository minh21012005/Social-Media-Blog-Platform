package com.socialmediablog.platform.services.interaction.domain.aggregate;

import com.socialmediablog.platform.services.interaction.domain.vo.InteractionId;
import java.util.UUID;

public class Interaction {

    private final InteractionId id;

    private Interaction(InteractionId id) {
        this.id = id;
    }

    public static Interaction restore(UUID id) {
        return new Interaction(InteractionId.of(id));
    }

    public InteractionId id() {
        return id;
    }
}
