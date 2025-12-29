package com.rushcrew.auth_service.auth.domain.exception;

import com.rushcrew.common.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    // 인증 에러
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH-001", "아이디 또는 비밀번호가 올바르지 않습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH-002", "이미 사용 중인 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-003", "사용자를 찾을 수 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-004", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH-005", "만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-006", "리프레시 토큰을 찾을 수 없습니다."),
    INVALID_TOKEN_EXPIRY(HttpStatus.BAD_REQUEST, "AUTH-007", "유효하지 않은 토큰 만료 시간입니다."),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "AUTH-008", "유효하지 않은 사용자 ID 입니다."),
    INVALID_TOKEN_ID(HttpStatus.BAD_REQUEST, "AUTH-009", "유효하지 않은 토큰 ID 입니다."),
    USER_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AUTH-010", "사용자 서비스를 일시적으로 사용할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String name;
    private final String message;
}
