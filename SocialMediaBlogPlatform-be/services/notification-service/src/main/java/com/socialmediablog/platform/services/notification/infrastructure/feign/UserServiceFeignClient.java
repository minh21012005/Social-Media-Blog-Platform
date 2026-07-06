package com.socialmediablog.platform.services.notification.infrastructure.feign;

import com.socialmediablog.platform.common.web.ApiResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/api/v1/users")
public interface UserServiceFeignClient {

    @GetMapping("/{userId}")
    ApiResponse<UserPublicProfileResponse> getPublicUser(@PathVariable("userId") UUID userId);
}
