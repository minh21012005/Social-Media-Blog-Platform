package com.socialmediablog.platform.services.notification.infrastructure.persistence;

import com.socialmediablog.platform.services.notification.infrastructure.entity.JpaNotificationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;

public interface SpringDataJpaNotificationRepository extends JpaRepository<JpaNotificationEntity, UUID> {

    List<JpaNotificationEntity> findByRecipientId(UUID recipientId);

    boolean existsByRecipientIdAndActorIdAndTypeAndSubjectId(UUID recipientId, UUID actorId, String type, UUID subjectId);

    @Modifying
    @Query("UPDATE JpaNotificationEntity n SET n.status = 'READ', n.readAt = :now, n.updatedAt = :now WHERE n.recipientId = :recipientId AND n.status = 'UNREAD'")
    int markAllAsReadByRecipientId(@Param("recipientId") UUID recipientId, @Param("now") Instant now);
}
