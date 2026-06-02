package com.socialmediablog.platform.services.article.application.port.out;

import com.socialmediablog.platform.common.events.DomainEvent;
import java.util.UUID;

public interface ArticleEventPublisher {

    void publish(UUID aggregateId, DomainEvent event);
}
