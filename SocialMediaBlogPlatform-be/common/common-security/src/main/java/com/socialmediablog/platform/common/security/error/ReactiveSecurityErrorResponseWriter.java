package com.socialmediablog.platform.common.security.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.common.web.error.ErrorCode;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public final class ReactiveSecurityErrorResponseWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ReactiveSecurityErrorResponseWriter() {
    }

    public static Mono<Void> write(ServerWebExchange exchange, HttpStatus status, ErrorCode code, String message) {
        byte[] bytes = responseBytes(status.value(), code, message);
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private static byte[] responseBytes(int status, ErrorCode code, String message) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(ApiResponse.failure(status, code, message));
        } catch (JsonProcessingException exception) {
            String fallback = """
                    {"success":false,"status":%d,"message":"%s","data":null}
                    """.formatted(status, message);
            return fallback.getBytes(StandardCharsets.UTF_8);
        }
    }
}
