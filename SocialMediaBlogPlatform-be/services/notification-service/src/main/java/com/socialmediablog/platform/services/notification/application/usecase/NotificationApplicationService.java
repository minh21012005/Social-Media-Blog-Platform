package com.socialmediablog.platform.services.notification.application.usecase;

import com.socialmediablog.platform.services.notification.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.notification.application.command.ListMyNotificationsCommand;
import com.socialmediablog.platform.services.notification.application.command.MarkNotificationReadCommand;
import com.socialmediablog.platform.services.notification.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.notification.application.port.in.ListMyNotificationsUseCase;
import com.socialmediablog.platform.services.notification.application.port.in.MarkNotificationReadUseCase;
import com.socialmediablog.platform.services.notification.application.result.NotificationItem;
import com.socialmediablog.platform.services.notification.application.result.ServiceStatus;
import com.socialmediablog.platform.services.notification.domain.aggregate.Notification;
import com.socialmediablog.platform.services.notification.domain.repository.NotificationRepository;
import com.socialmediablog.platform.services.notification.domain.vo.NotificationId;
import com.socialmediablog.platform.services.notification.domain.vo.RecipientId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationApplicationService
        implements GetServiceStatusUseCase, ListMyNotificationsUseCase, MarkNotificationReadUseCase {

    private final NotificationRepository notificationRepository;

    public NotificationApplicationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceStatus execute(GetServiceStatusCommand command) {
        return new ServiceStatus("notification-service", "notifications", command.currentUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationItem> execute(ListMyNotificationsCommand command) {
        RecipientId recipientId = RecipientId.of(command.currentUserId());
        return notificationRepository.findByRecipientId(recipientId).stream()
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
                .map(NotificationItem::from)
                .toList();
    }

    @Override
    @Transactional
    public NotificationItem execute(MarkNotificationReadCommand command) {
        NotificationId notificationId = NotificationId.of(command.notificationId());
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + command.notificationId()));

        // Chỉ owner mới được đánh dấu đã đọc notification của mình
        UUID recipientId = notification.recipientId().value();
        if (!recipientId.equals(command.currentUserId())) {
            throw new IllegalArgumentException("Forbidden: notification does not belong to current user");
        }

        Notification updated = notification.markRead(Instant.now());
        return NotificationItem.from(notificationRepository.save(updated));
    }
}
