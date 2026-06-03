package com.socialmediablog.platform.services.comment.api.exception;

import com.socialmediablog.platform.common.web.ApiResponse;
import com.socialmediablog.platform.common.web.error.ErrorCode;
import com.socialmediablog.platform.services.comment.application.exception.CommentAlreadyDeletedException;
import com.socialmediablog.platform.services.comment.application.exception.CommentNotFoundException;
import com.socialmediablog.platform.services.comment.application.exception.CommentPermissionDeniedException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ApiResponse.failure(ErrorCode.VALIDATION_ERROR, message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> illegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(ErrorCode.BAD_REQUEST, exception.getMessage()));
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> commentNotFound(CommentNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(ErrorCode.NOT_FOUND, exception.getMessage()));
    }

    @ExceptionHandler(CommentPermissionDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> permissionDenied(CommentPermissionDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.failure(ErrorCode.FORBIDDEN, exception.getMessage()));
    }

    @ExceptionHandler(CommentAlreadyDeletedException.class)
    public ResponseEntity<ApiResponse<Void>> alreadyDeleted(CommentAlreadyDeletedException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(ErrorCode.CONFLICT, exception.getMessage()));
    }
}
