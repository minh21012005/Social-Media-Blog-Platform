package com.socialmediablog.platform.services.comment.application.result;

import java.util.List;

public record PageResult<T>(List<T> items, int page, int size, long totalItems, int totalPages) {
    public static <T> PageResult<T> of(List<T> items, int page, int size, long totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / size);
        return new PageResult<>(items, page, size, totalItems, totalPages);
    }
}
