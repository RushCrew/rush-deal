package com.rushcrew.user_service.global.exception;

import com.rushcrew.common.dto.ApiResponse;
import com.rushcrew.common.dto.ErrorResponse;
import com.rushcrew.user_service.user.domain.error.UserErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleAccessDeniedException(AccessDeniedException e) {
        ErrorResponse errorResponse = ErrorResponse.of(UserErrorCode.ACCESS_DENIED);
        ApiResponse<ErrorResponse> response = ApiResponse.error(errorResponse);

        return ResponseEntity.status(
            UserErrorCode.ACCESS_DENIED.getHttpStatus()
        ).body(response);
    }
}
