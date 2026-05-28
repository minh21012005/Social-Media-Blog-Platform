package com.socialmediablog.platform.services.follower.infrastructure.persistence;

import com.socialmediablog.platform.services.follower.infrastructure.entity.JpaOutboxEventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaOutboxEventRepository extends JpaRepository<JpaOutboxEventEntity, UUID> {
}
