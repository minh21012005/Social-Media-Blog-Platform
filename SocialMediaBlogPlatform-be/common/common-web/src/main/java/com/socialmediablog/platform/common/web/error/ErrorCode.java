package com.socialmediablog.platform.common.web.error;

public enum ErrorCode {
    VALIDATION_ERROR(400, "Validation failed"),
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Authentication is required"),
    FORBIDDEN(403, "Access is denied"),
    NOT_FOUND(404, "Resource was not found"),
    CONFLICT(409, "Resource conflict"),
    INTERNAL_SERVER_ERROR(500, "Internal server error");

    private final int defaultStatus;
    private final String defaultMessage;

    ErrorCode(int defaultStatus, String defaultMessage) {
        this.defaultStatus = defaultStatus;
        this.defaultMessage = defaultMessage;
    }

    public int defaultStatus() {
        return defaultStatus;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
