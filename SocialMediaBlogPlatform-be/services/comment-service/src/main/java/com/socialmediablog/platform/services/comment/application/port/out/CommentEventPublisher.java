package com.socialmediablog.platform.services.comment.application.port.out;

import com.socialmediablog.platform.common.events.DomainEvent;
import java.util.UUID;

public interface CommentEventPublisher {

    void publish(UUID aggregateId, DomainEvent event);
}
