package com.socialmediablog.platform.services.notification.infrastructure.feign;

import com.socialmediablog.platform.common.web.ApiResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for follower-service.
 * Calls GET /api/v1/follows/{userId}/followers to get all followers of a user.
 * This endpoint does NOT require authentication.
 */
@FeignClient(name = "follower-service")
public interface FollowerServiceFeignClient {

    @GetMapping("/api/v1/follows/{userId}/followers")
    ApiResponse<FollowerPage> getFollowers(
            @PathVariable("userId") UUID userId,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );
}

