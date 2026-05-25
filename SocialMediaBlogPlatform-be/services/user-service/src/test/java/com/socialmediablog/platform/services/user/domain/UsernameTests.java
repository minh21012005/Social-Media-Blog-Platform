package com.socialmediablog.platform.services.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class UsernameTests {

    @Test
    void normalizesUsername() {
        assertThat(Username.of("Mai.Writer").value()).isEqualTo("mai.writer");
    }

    @Test
    void rejectsInvalidUsername() {
        assertThatThrownBy(() -> Username.of("ma"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
