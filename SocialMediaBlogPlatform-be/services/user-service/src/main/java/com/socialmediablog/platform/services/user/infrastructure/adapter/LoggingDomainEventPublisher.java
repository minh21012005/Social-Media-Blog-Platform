package com.socialmediablog.platform.services.user.infrastructure.adapter;

import com.socialmediablog.platform.common.events.DomainEvent;
import com.socialmediablog.platform.services.user.application.port.out.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingDomainEventPublisher.class);

    @Override
    public void publish(DomainEvent event) {
        log.info("Domain event published: type={}, id={}", event.eventType(), event.eventId());
    }
}
