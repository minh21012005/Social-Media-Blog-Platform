package com.socialmediablog.platform.services.notification.api.controller;

import com.socialmediablog.platform.common.security.CurrentUser;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.services.notification.api.dto.ServiceStatusResponse;
import com.socialmediablog.platform.services.notification.application.command.GetServiceStatusCommand;
import com.socialmediablog.platform.services.notification.application.command.ListMyNotificationsCommand;
import com.socialmediablog.platform.services.notification.application.command.MarkNotificationReadCommand;
import com.socialmediablog.platform.services.notification.application.port.in.GetServiceStatusUseCase;
import com.socialmediablog.platform.services.notification.application.port.in.ListMyNotificationsUseCase;
import com.socialmediablog.platform.services.notification.application.port.in.MarkNotificationReadUseCase;
import com.socialmediablog.platform.services.notification.application.port.in.MarkAllNotificationsReadUseCase;
import com.socialmediablog.platform.services.notification.application.command.MarkAllNotificationsReadCommand;
import com.socialmediablog.platform.services.notification.application.command.DeleteNotificationCommand;
import com.socialmediablog.platform.services.notification.application.port.in.DeleteNotificationUseCase;
import com.socialmediablog.platform.services.notification.application.result.NotificationItem;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final GetServiceStatusUseCase getServiceStatusUseCase;
    private final ListMyNotificationsUseCase listMyNotificationsUseCase;
    private final MarkNotificationReadUseCase markNotificationReadUseCase;
    private final MarkAllNotificationsReadUseCase markAllNotificationsReadUseCase;
    private final DeleteNotificationUseCase deleteNotificationUseCase;

    public NotificationController(
            GetServiceStatusUseCase getServiceStatusUseCase,
            ListMyNotificationsUseCase listMyNotificationsUseCase,
            MarkNotificationReadUseCase markNotificationReadUseCase,
            MarkAllNotificationsReadUseCase markAllNotificationsReadUseCase,
            DeleteNotificationUseCase deleteNotificationUseCase
    ) {
        this.getServiceStatusUseCase = getServiceStatusUseCase;
        this.listMyNotificationsUseCase = listMyNotificationsUseCase;
        this.markNotificationReadUseCase = markNotificationReadUseCase;
        this.markAllNotificationsReadUseCase = markAllNotificationsReadUseCase;
        this.deleteNotificationUseCase = deleteNotificationUseCase;
    }

    @GetMapping("/status")
    public ApiResponse<ServiceStatusResponse> status(@AuthenticationPrincipal CurrentUser currentUser) {
        return ApiResponse.success(ServiceStatusResponse.from(getServiceStatusUseCase.execute(
                new GetServiceStatusCommand(currentUser.id())
        )));
    }

    /**
     * GET /api/v1/notifications/me
     * Trả về danh sách notification của user hiện tại, sắp xếp mới nhất lên trên.
     */
    @GetMapping("/me")
    public ApiResponse<List<NotificationItem>> myNotifications(@AuthenticationPrincipal CurrentUser currentUser) {
        UUID userId = UUID.fromString(currentUser.id());
        List<NotificationItem> items = listMyNotificationsUseCase.execute(
                new ListMyNotificationsCommand(userId)
        );
        return ApiResponse.success(items);
    }

    /**
     * PATCH /api/v1/notifications/{id}/read
     * Đánh dấu một notification là đã đọc.
     */
    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationItem> markRead(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID id
    ) {
        UUID userId = UUID.fromString(currentUser.id());
        NotificationItem updated = markNotificationReadUseCase.execute(
                new MarkNotificationReadCommand(userId, id)
        );
        return ApiResponse.success(updated);
    }

    /**
     * PATCH /api/v1/notifications/read-all
     * Đánh dấu tất cả notification của user là đã đọc.
     */
    @PatchMapping("/read-all")
    public ApiResponse<Integer> markAllRead(@AuthenticationPrincipal CurrentUser currentUser) {
        UUID userId = UUID.fromString(currentUser.id());
        int updatedCount = markAllNotificationsReadUseCase.execute(
                new MarkAllNotificationsReadCommand(userId)
        );
        return ApiResponse.success(updatedCount);
    }

    /**
     * DELETE /api/v1/notifications/{id}
     * Xóa một notification.
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNotification(
            @AuthenticationPrincipal CurrentUser currentUser,
            @PathVariable UUID id
    ) {
        UUID userId = UUID.fromString(currentUser.id());
        deleteNotificationUseCase.execute(
                new DeleteNotificationCommand(userId, id)
        );
        return ApiResponse.success(null);
    }
}
