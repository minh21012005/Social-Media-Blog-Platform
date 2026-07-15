package com.socialmediablog.platform.services.notification.application.port.in;

import com.socialmediablog.platform.services.notification.application.command.DeleteNotificationCommand;

public interface DeleteNotificationUseCase {
    void execute(DeleteNotificationCommand command);
}
