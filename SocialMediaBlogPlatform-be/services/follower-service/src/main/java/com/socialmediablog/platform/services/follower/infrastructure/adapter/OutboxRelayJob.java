package com.socialmediablog.platform.services.follower.infrastructure.adapter;

import com.socialmediablog.platform.services.follower.infrastructure.entity.JpaOutboxEventEntity;
import com.socialmediablog.platform.services.follower.infrastructure.persistence.SpringDataJpaOutboxEventRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxRelayJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayJob.class);

    private final SpringDataJpaOutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;
    private final int batchSize;

    public OutboxRelayJob(
            SpringDataJpaOutboxEventRepository repository,
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${outbox.relay.topic}") String topic,
            @Value("${outbox.relay.batch-size:100}") int batchSize
    ) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${outbox.relay.fixed-delay-ms:5000}")
    @Transactional
    public void relay() {
        List<JpaOutboxEventEntity> pending = repository.findPendingForUpdate(batchSize);
        if (pending.isEmpty()) {
            return;
        }

        log.debug("[OutboxRelay][follower-service] Processing {} pending event(s)", pending.size());

        for (JpaOutboxEventEntity event : pending) {
            try {
                kafkaTemplate.send(topic, event.id().toString(), event.getPayload()).get();
                event.markCompleted(Instant.now());
                repository.save(event);
                log.debug("[OutboxRelay][follower-service] Published event id={} type={}", event.id(), event.getEventType());
            } catch (Exception ex) {
                log.error("[OutboxRelay][follower-service] Failed to publish event id={} type={}: {}",
                        event.id(), event.getEventType(), ex.getMessage());
            }
        }
    }
}
