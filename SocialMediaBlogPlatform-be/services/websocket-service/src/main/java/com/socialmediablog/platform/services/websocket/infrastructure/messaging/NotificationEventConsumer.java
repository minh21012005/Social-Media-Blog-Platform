package com.socialmediablog.platform.services.websocket.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationEventConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "notification.events", groupId = "websocket-service-group")
    public void consume(NotificationPushEvent event) {
        log.debug("[WebSocket] Received notification.events for user: {}", event.recipientId());

        try {
            // Push to the specific user's queue
            // Spring STOMP maps this to: /user/{recipientId}/queue/notifications
            messagingTemplate.convertAndSendToUser(
                    event.recipientId().toString(),
                    "/queue/notifications",
                    event
            );
            log.debug("[WebSocket] Pushed notification to user {} successfully", event.recipientId());
        } catch (Exception e) {
            log.error("[WebSocket] Error pushing notification to user {}: {}", event.recipientId(), e.getMessage());
        }
    }
}
