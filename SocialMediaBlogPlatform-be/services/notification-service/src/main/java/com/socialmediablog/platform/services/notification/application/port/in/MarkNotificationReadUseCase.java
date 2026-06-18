package com.socialmediablog.platform.services.notification.application.port.in;

import com.socialmediablog.platform.services.notification.application.command.MarkNotificationReadCommand;
import com.socialmediablog.platform.services.notification.application.result.NotificationItem;

public interface MarkNotificationReadUseCase {
    NotificationItem execute(MarkNotificationReadCommand command);
}
