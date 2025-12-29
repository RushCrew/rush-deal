package com.rushcrew.order_service.presentation.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.rushcrew.order_service.application.command.dto.result.CancelOrderResult;

public record CancelOrderResponse(
	UUID orderId,
	String orderStatus,
	Instant cancelledAt,
	String message
) {
	public static CancelOrderResponse from(CancelOrderResult result) {
		return new CancelOrderResponse(
			result.orderId(),
			result.orderStatus(),
			result.cancelledAt(),
			"주문이 취소되었습니다."
		);
	}
}
