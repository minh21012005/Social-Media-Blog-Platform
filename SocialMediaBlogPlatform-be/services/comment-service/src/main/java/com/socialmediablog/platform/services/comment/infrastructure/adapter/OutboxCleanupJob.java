package com.socialmediablog.platform.services.comment.infrastructure.adapter;

import com.socialmediablog.platform.services.comment.infrastructure.persistence.SpringDataJpaOutboxEventRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxCleanupJob.class);

    private final SpringDataJpaOutboxEventRepository repository;
    private final int retentionDays;

    public OutboxCleanupJob(
            SpringDataJpaOutboxEventRepository repository,
            @Value("${outbox.cleanup.retention-days:30}") int retentionDays
    ) {
        if (retentionDays < 1) {
            throw new IllegalArgumentException("Outbox retention days must be at least 1");
        }
        this.repository = repository;
        this.retentionDays = retentionDays;
    }

    @Scheduled(
            cron = "${outbox.cleanup.cron:0 0 3 * * *}",
            zone = "${outbox.cleanup.zone:UTC}"
    )
    @Transactional
    public void cleanup() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        long deleted = repository.deleteByStatusAndPublishedAtBefore("COMPLETED", cutoff);
        if (deleted > 0) {
            log.info("[OutboxCleanup][comment-service] Deleted {} completed event(s) older than {}", deleted, cutoff);
        }
    }
}