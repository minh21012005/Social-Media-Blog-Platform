package com.socialmediablog.platform.services.websocket.infrastructure.presence;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class PresenceEventListener {

    private static final Logger log = LoggerFactory.getLogger(PresenceEventListener.class);
    private static final String PRESENCE_KEY_PREFIX = "presence:user:";
    private static final Duration PRESENCE_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public PresenceEventListener(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        if (event.getUser() == null || event.getUser().getName() == null) {
            return;
        }
        
        String userId = event.getUser().getName();
        String redisKey = PRESENCE_KEY_PREFIX + userId;
        
        log.debug("User {} connected. Saving presence to Redis.", userId);
        redisTemplate.opsForValue().increment(redisKey);
        redisTemplate.expire(redisKey, PRESENCE_TTL);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        if (event.getUser() == null || event.getUser().getName() == null) {
            return;
        }
        
        String userId = event.getUser().getName();
        String redisKey = PRESENCE_KEY_PREFIX + userId;
        
        log.debug("User {} disconnected. Removing presence from Redis.", userId);
        Long count = redisTemplate.opsForValue().decrement(redisKey);
        if (count != null && count <= 0) {
            redisTemplate.delete(redisKey);
        }
    }
}
