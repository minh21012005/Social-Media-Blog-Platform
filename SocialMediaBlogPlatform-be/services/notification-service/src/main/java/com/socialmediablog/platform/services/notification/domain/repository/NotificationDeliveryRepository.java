package com.socialmediablog.platform.services.notification.domain.repository;

import com.socialmediablog.platform.services.notification.domain.aggregate.NotificationDelivery;
import com.socialmediablog.platform.services.notification.domain.vo.NotificationId;
import java.util.List;

public interface NotificationDeliveryRepository {

    List<NotificationDelivery> findByNotificationId(NotificationId notificationId);

    NotificationDelivery save(NotificationDelivery delivery);
}
