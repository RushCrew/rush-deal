package com.rushcrew.order_service.presentation.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import com.rushcrew.order_service.application.command.dto.result.ConfirmPurchaseResult;

public record ConfirmPurchaseResponse(
	UUID orderId,
	String orderStatus,
	LocalDateTime purchaseConfirmedAt,
	String message
) {
	public static ConfirmPurchaseResponse from(ConfirmPurchaseResult result) {
		return new ConfirmPurchaseResponse(
			result.orderId(),
			result.orderStatus(),
			toLocalDateTime(result.purchaseConfirmedAt()),
			"구매가 확정되었습니다."
		);
	}

	private static LocalDateTime toLocalDateTime(Instant instant) {
		return instant != null
			? LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Seoul"))
			: null;
	}
}
