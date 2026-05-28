package com.socialmediablog.platform.services.notification.infrastructure.entity;

import com.socialmediablog.platform.common.web.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class JpaOutboxEventEntity extends BaseEntity {

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 80)
    private String aggregateType;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected JpaOutboxEventEntity() {
    }
}
