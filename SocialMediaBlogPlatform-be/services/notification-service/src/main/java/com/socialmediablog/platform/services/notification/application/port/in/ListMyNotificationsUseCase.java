package com.socialmediablog.platform.services.notification.application.port.in;

import com.socialmediablog.platform.services.notification.application.command.ListMyNotificationsCommand;
import com.socialmediablog.platform.services.notification.application.result.NotificationItem;
import java.util.List;

public interface ListMyNotificationsUseCase {
    List<NotificationItem> execute(ListMyNotificationsCommand command);
}
