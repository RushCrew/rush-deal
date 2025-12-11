package com.rushcrew.user_service.point.exception;

import com.rushcrew.common.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PointErrorCode implements ErrorCode {

    INVALID_POINT_AMOUNT(HttpStatus.BAD_REQUEST, "POINT-001", "유효하지 않은 포인트 금액입니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "POINT-002", "포인트 잔액이 부족합니다."),
    INVALID_ORDER_ID(HttpStatus.BAD_REQUEST, "POINT-010", "유효하지 않은 주문 ID 입니다."),
    INVALID_POINT_PARAMETER(HttpStatus.BAD_REQUEST, "POINT-011", "유효하지 않은 포인트 파라미터입니다."),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "POINT-012", "유효하지 않은 사용자 ID 입니다."),
    LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT, "POINT-013", "요청 폭주로 처리 실패했습니다."),

    DUPLICATE_ORDER_PROCESSING(HttpStatus.CONFLICT, "POINT-003", "이미 처리된 주문입니다."),
    DUPLICATE_POINT_EARN(HttpStatus.CONFLICT, "POINT-004", "이미 적립된 주문입니다."),
    POINT_HISTORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "POINT-005", "취소할 포인트 이력이 존재하지 않습니다."),
    UNAUTHORIZED_POINT_ACCESS(HttpStatus.FORBIDDEN, "POINT-006", "해당 포인트 이력에 대한 권한이 없습니다."),
    POINT_ALREADY_CONFIRMED(HttpStatus.BAD_REQUEST, "POINT-007", "이미 확정된 적립은 취소할 수 없습니다."),
    POINT_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "POINT-008", "이미 취소된 주문입니다."),
    POINT_INVALID_STATUS_FOR_CONFIRM(HttpStatus.BAD_REQUEST, "POINT-009", "적립 대기 상태만 확정할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final String name;
    private final String message;
}
