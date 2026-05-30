package com.socialmediablog.platform.common.security.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.common.web.error.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public final class ServletSecurityErrorResponseWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ServletSecurityErrorResponseWriter() {
    }

    public static void write(HttpServletResponse response, HttpStatus status, ErrorCode code, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        OBJECT_MAPPER.writeValue(response.getOutputStream(), ApiResponse.failure(status.value(), code, message));
    }
}
