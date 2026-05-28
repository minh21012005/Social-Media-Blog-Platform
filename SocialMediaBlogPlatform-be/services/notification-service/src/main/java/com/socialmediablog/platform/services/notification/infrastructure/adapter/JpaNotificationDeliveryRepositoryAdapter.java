package com.socialmediablog.platform.services.notification.infrastructure.adapter;

import com.socialmediablog.platform.services.notification.domain.aggregate.NotificationDelivery;
import com.socialmediablog.platform.services.notification.domain.repository.NotificationDeliveryRepository;
import com.socialmediablog.platform.services.notification.domain.vo.NotificationId;
import com.socialmediablog.platform.services.notification.infrastructure.entity.JpaNotificationDeliveryEntity;
import com.socialmediablog.platform.services.notification.infrastructure.persistence.SpringDataJpaNotificationDeliveryRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class JpaNotificationDeliveryRepositoryAdapter implements NotificationDeliveryRepository {

    private final SpringDataJpaNotificationDeliveryRepository repository;

    public JpaNotificationDeliveryRepositoryAdapter(SpringDataJpaNotificationDeliveryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<NotificationDelivery> findByNotificationId(NotificationId notificationId) {
        return repository.findByNotificationId(notificationId.value()).stream()
                .map(JpaNotificationDeliveryEntity::toDomain)
                .toList();
    }

    @Override
    public NotificationDelivery save(NotificationDelivery delivery) {
        return repository.save(JpaNotificationDeliveryEntity.fromDomain(delivery)).toDomain();
    }
}
