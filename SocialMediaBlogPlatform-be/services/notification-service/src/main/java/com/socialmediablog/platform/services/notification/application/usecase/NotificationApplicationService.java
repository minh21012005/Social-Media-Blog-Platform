package com.socialmediablog.platform.services.notification.application.usecase;

import com.socialmediablog.platform.services.notification.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.notification.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.notification.application.result.ServiceStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationApplicationService implements GetServiceStatusUseCase {

    @Override
    @Transactional(readOnly = true)
    public ServiceStatus execute(GetServiceStatusCommand command) {
        return new ServiceStatus("notification-service", "notifications", command.currentUserId());
    }
}
