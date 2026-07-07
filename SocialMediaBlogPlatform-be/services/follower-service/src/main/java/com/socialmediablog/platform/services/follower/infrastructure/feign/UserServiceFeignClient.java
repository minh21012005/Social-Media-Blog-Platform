package com.socialmediablog.platform.services.follower.infrastructure.feign;

import com.socialmediablog.platform.common.web.ApiResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceFeignClient {

    @GetMapping("/api/v1/users/{userId}")
    ApiResponse<UserResponse> getPublicUser(@PathVariable("userId") UUID userId);

    record UserResponse(
            UUID id,
            String username,
            String displayName,
            String bio,
            String avatarUrl,
            boolean isPrivate
    ) {}
}
