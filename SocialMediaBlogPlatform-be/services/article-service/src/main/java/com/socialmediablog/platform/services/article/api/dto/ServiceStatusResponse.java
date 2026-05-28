package com.socialmediablog.platform.services.article.api.dto;

import com.socialmediablog.platform.services.article.application.result.ServiceStatus;

public record ServiceStatusResponse(String service, String boundedContext, String currentUserId) {

    public static ServiceStatusResponse from(ServiceStatus status) {
        return new ServiceStatusResponse(status.service(), status.boundedContext(), status.currentUserId());
    }
}
