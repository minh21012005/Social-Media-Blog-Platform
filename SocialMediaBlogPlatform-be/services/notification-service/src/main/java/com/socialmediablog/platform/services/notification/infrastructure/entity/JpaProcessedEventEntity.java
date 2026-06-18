package com.socialmediablog.platform.services.notification.infrastructure.entity;

import com.socialmediablog.platform.common.web.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
public class JpaProcessedEventEntity extends BaseEntity {

    protected JpaProcessedEventEntity() {
    }

    public JpaProcessedEventEntity(UUID id) {
        super(id, Instant.now(), Instant.now());
    }
}
