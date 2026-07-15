package com.socialmediablog.platform.services.notification.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.socialmediablog.platform.services.notification.domain.aggregate.Notification;
import com.socialmediablog.platform.services.notification.domain.aggregate.NotificationDelivery;
import com.socialmediablog.platform.services.notification.domain.model.NotificationChannel;
import com.socialmediablog.platform.services.notification.domain.model.NotificationType;
import com.socialmediablog.platform.services.notification.domain.vo.RecipientId;
import com.socialmediablog.platform.services.notification.infrastructure.entity.JpaNotificationDeliveryEntity;
import com.socialmediablog.platform.services.notification.infrastructure.entity.JpaNotificationEntity;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:notification_persistence;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotificationPersistenceTests {

    private static final Instant NOW = Instant.parse("2026-05-28T00:00:00Z");

    @Autowired
    private SpringDataJpaNotificationRepository notificationRepository;

    @Autowired
    private SpringDataJpaNotificationDeliveryRepository deliveryRepository;

    @Test
    void mapsNotificationAndDeliveryDomainAndJpaBothWays() {
        Notification notification = Notification.create(
                RecipientId.of(UUID.randomUUID()),
                UUID.randomUUID(),
                NotificationType.COMMENT_CREATED,
                "ARTICLE",
                UUID.randomUUID(),
                "New comment",
                "Someone commented on your article.",
                NOW
        );

        Notification mapped = JpaNotificationEntity.fromDomain(notification).toDomain();
        assertThat(mapped.id()).isEqualTo(notification.id());
        assertThat(mapped.recipientId()).isEqualTo(notification.recipientId());
        assertThat(mapped.type()).isEqualTo(NotificationType.COMMENT_CREATED);
        assertThat(mapped.title()).isEqualTo("New comment");

        notificationRepository.saveAndFlush(JpaNotificationEntity.fromDomain(notification));
        NotificationDelivery delivery = NotificationDelivery.pending(notification.id(), NotificationChannel.IN_APP, NOW);
        NotificationDelivery mappedDelivery = deliveryRepository.saveAndFlush(JpaNotificationDeliveryEntity.fromDomain(delivery)).toDomain();

        assertThat(mappedDelivery.notificationId()).isEqualTo(notification.id());
        assertThat(deliveryRepository.findByNotificationId(notification.id().value())).hasSize(1);
    }

}
