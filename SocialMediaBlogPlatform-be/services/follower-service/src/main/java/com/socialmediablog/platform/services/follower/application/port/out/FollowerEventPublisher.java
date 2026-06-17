package com.socialmediablog.platform.services.follower.application.port.out;

import com.socialmediablog.platform.common.events.DomainEvent;
import java.util.UUID;

public interface FollowerEventPublisher {

    void publish(UUID aggregateId, DomainEvent event);
}
