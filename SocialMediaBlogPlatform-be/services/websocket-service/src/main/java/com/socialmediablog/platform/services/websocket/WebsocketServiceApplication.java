package com.socialmediablog.platform.services.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {
        "com.socialmediablog.platform.common.web",
        "com.socialmediablog.platform.common.security",
        "com.socialmediablog.platform.services.websocket"
})
public class WebsocketServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebsocketServiceApplication.class, args);
    }
}
