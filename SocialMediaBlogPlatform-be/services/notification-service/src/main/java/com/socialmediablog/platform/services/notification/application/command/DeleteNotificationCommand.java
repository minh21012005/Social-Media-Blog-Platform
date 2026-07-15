package com.socialmediablog.platform.services.notification.application.command;

import java.util.UUID;

public record DeleteNotificationCommand(UUID currentUserId, UUID notificationId) {
}
