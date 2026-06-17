package com.socialmediablog.platform.common.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JacksonRecordTest {

    record TestEventWithoutAnnotation(UUID eventId) implements DomainEvent {
        @Override
        public String eventType() {
            return "test.event";
        }
        @Override
        public Instant occurredAt() {
            return Instant.now();
        }
    }

    @Test
    void testJacksonSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        TestEventWithoutAnnotation event = new TestEventWithoutAnnotation(UUID.randomUUID());
        String json = mapper.writeValueAsString(event);
        
        System.out.println("JSON Output: " + json);
        
        // This will verify if eventType is present or not
        assertFalse(json.contains("eventType"), "If this passes, it proves eventType is MISSING without annotation!");
    }
}
