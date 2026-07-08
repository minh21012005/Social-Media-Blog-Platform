package com.socialmediablog.platform.services.interaction.application.port.out;

import com.socialmediablog.platform.common.events.DomainEvent;
import java.util.UUID;

public interface InteractionEventPublisher {

    void publish(UUID aggregateId, DomainEvent event);
}
