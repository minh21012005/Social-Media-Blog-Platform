package com.socialmediablog.platform.services.interaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class InteractionServiceApplication {

    public static void main(String[] args) {
        normalizeJvmTimezone();
        SpringApplication.run(InteractionServiceApplication.class, args);
    }

    private static void normalizeJvmTimezone() {
        TimeZone current = TimeZone.getDefault();
        if ("Asia/Saigon".equals(current.getID())) {
            // PostgreSQL rejects this deprecated alias in some environments.
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        }
    }
}
