package com.socialmediablog.platform.services.notification.application.command;

import java.util.UUID;

public record MarkNotificationReadCommand(UUID currentUserId, UUID notificationId) {
}
