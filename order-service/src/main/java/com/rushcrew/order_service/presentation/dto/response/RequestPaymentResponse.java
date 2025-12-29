package com.rushcrew.order_service.presentation.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import com.rushcrew.order_service.application.command.dto.result.RequestPaymentResult;

public record RequestPaymentResponse(
	UUID orderId,
	String orderStatus,
	BigDecimal paymentAmount,
	LocalDateTime paymentCompletedAt,
	LocalDateTime autoConfirmScheduledAt,
	String message
) {
	public static RequestPaymentResponse from(RequestPaymentResult result) {
		return new RequestPaymentResponse(
			result.orderId(),
			result.orderStatus(),
			result.paymentAmount(),
			toLocalDateTime(result.paymentCompletedAt()),
			toLocalDateTime(result.autoConfirmScheduledAt()),
			"결제가 완료되었습니다. 7일 후 자동으로 구매 확정됩니다."
		);
	}

	private static LocalDateTime toLocalDateTime(Instant instant) {
		return instant != null
			? LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Seoul"))
			: null;
	}
}
