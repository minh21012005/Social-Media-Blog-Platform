package com.socialmediablog.platform.services.follower;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FollowerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FollowerServiceApplication.class, args);
    }
}
