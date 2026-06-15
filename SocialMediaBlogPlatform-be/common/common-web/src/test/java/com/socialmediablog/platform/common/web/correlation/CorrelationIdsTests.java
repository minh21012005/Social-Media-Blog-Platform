package com.socialmediablog.platform.common.web.correlation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class CorrelationIdsTests {

    @Test
    void keepsSafeCorrelationId() {
        assertEquals("request-123_test:value", CorrelationIds.resolve(" request-123_test:value "));
    }

    @Test
    void generatesCorrelationIdWhenMissing() {
        assertFalse(CorrelationIds.resolve(null).isBlank());
    }

    @Test
    void generatesCorrelationIdWhenUnsafe() {
        String resolved = CorrelationIds.resolve("bad header value");

        assertNotEquals("bad header value", resolved);
        assertFalse(resolved.isBlank());
    }

    @Test
    void generatesCorrelationIdWhenTooLong() {
        String tooLong = "a".repeat(CorrelationIds.MAX_LENGTH + 1);

        String resolved = CorrelationIds.resolve(tooLong);

        assertNotEquals(tooLong, resolved);
        assertFalse(resolved.isBlank());
    }
}
