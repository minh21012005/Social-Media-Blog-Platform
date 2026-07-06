package com.socialmediablog.platform.services.comment.api.dto;

import com.socialmediablog.platform.services.comment.application.result.CommentView;
import com.socialmediablog.platform.services.comment.application.result.PageResult;
import java.util.List;

public record CommentPageResponse(
        List<CommentResponse> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {
    public static CommentPageResponse from(PageResult<CommentView> result) {
        return new CommentPageResponse(
                result.items().stream().map(CommentResponse::from).toList(),
                result.page(),
                result.size(),
                result.totalItems(),
                result.totalPages()
        );
    }
}
