package com.socialmediablog.platform.services.notification.api.controller;

import com.socialmediablog.platform.common.security.CurrentUser;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.notification.api.dto.ServiceStatusResponse;
import com.socialmediablog.platform.services.notification.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.notification.application.port.in.GetServiceStatusUseCase;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final GetServiceStatusUseCase getServiceStatusUseCase;

    public NotificationController(GetServiceStatusUseCase getServiceStatusUseCase) {
        this.getServiceStatusUseCase = getServiceStatusUseCase;
    }

    @GetMapping("/status")
    public ApiResponse<ServiceStatusResponse> status(@AuthenticationPrincipal CurrentUser currentUser) {
        return ApiResponse.success(ServiceStatusResponse.from(getServiceStatusUseCase.execute(
                new GetServiceStatusCommand(currentUser.id())
        )));
    }
}
