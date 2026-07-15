package com.socialmediablog.platform.services.notification.infrastructure.adapter;

import com.socialmediablog.platform.services.notification.infrastructure.persistence.SpringDataJpaProcessedEventRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProcessedEventCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(ProcessedEventCleanupJob.class);

    private final SpringDataJpaProcessedEventRepository repository;
    private final int retentionDays;

    public ProcessedEventCleanupJob(
            SpringDataJpaProcessedEventRepository repository,
            @Value("${processed-events.cleanup.retention-days:90}") int retentionDays
    ) {
        if (retentionDays < 1) {
            throw new IllegalArgumentException("Processed-event retention days must be at least 1");
        }
        this.repository = repository;
        this.retentionDays = retentionDays;
    }

    @Scheduled(
            cron = "${processed-events.cleanup.cron:0 30 3 * * *}",
            zone = "${processed-events.cleanup.zone:UTC}"
    )
    @Transactional
    public void cleanup() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        long deleted = repository.deleteByCreatedAtBefore(cutoff);
        if (deleted > 0) {
            log.info("[ProcessedEventCleanup][notification-service] Deleted {} event(s) older than {}", deleted, cutoff);
        }
    }
}