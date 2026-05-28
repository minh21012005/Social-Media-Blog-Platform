package com.socialmediablog.platform.services.comment.infrastructure.persistence;

import com.socialmediablog.platform.services.comment.infrastructure.entity.JpaOutboxEventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaOutboxEventRepository extends JpaRepository<JpaOutboxEventEntity, UUID> {
}
