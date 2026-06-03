package com.socialmediablog.platform.services.interaction.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "likes")
public class LikeEntity {

    @EmbeddedId
    private LikeEntityId id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public LikeEntity() {
    }

    public LikeEntity(LikeEntityId id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public LikeEntityId getId() {
        return id;
    }

    public void setId(LikeEntityId id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
