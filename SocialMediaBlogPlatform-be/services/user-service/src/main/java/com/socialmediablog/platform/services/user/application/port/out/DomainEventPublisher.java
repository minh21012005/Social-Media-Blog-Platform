package com.socialmediablog.platform.services.user.application.port.out;

import com.socialmediablog.platform.common.events.DomainEvent;

public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
