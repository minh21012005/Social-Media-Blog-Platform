package com.socialmediablog.platform.services.websocket.api.controller;

import com.socialmediablog.platform.common.web.ApiResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/presence")
public class PresenceController {

    private static final String PRESENCE_KEY_PREFIX = "presence:user:";
    private final StringRedisTemplate redisTemplate;

    public PresenceController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping
    public ApiResponse<Map<UUID, Boolean>> getPresence(@RequestParam List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return ApiResponse.success(Map.of());
        }

        // Prepare Redis keys
        List<String> keys = userIds.stream()
            .map(id -> PRESENCE_KEY_PREFIX + id.toString())
            .toList();

        // MGET from Redis
        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        // Map results back to UUIDs
        Map<UUID, Boolean> result = IntStream.range(0, userIds.size())
            .boxed()
            .collect(Collectors.toMap(
                userIds::get,
                i -> {
                    String val = values == null ? null : values.get(i);
                    try {
                        return val != null && Integer.parseInt(val) > 0;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            ));

        return ApiResponse.success(result);
    }
}
