package com.socialmediablog.platform.services.notification.infrastructure.persistence;

import com.socialmediablog.platform.services.notification.infrastructure.entity.JpaProcessedEventEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataJpaProcessedEventRepository extends JpaRepository<JpaProcessedEventEntity, UUID> {

    long deleteByCreatedAtBefore(Instant cutoff);
}
