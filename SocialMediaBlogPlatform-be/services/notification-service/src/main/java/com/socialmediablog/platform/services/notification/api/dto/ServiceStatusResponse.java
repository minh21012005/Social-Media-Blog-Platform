package com.socialmediablog.platform.services.notification.api.dto;

import com.socialmediablog.platform.services.notification.application.result.ServiceStatus;

public record ServiceStatusResponse(String service, String context, String currentUserId) {

    public static ServiceStatusResponse from(ServiceStatus status) {
        return new ServiceStatusResponse(status.service(), status.context(), status.currentUserId());
    }
}
