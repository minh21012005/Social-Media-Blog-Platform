package com.socialmediablog.platform.services.notification.application.port.in;

import com.socialmediablog.platform.services.notification.application.command.MarkAllNotificationsReadCommand;

public interface MarkAllNotificationsReadUseCase {
    int execute(MarkAllNotificationsReadCommand command);
}
