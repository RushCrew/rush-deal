package com.rushcrew.order_service.application.port.out;

import java.math.BigDecimal;

import lombok.NonNull;

public interface PointPort {

	boolean deductPoint(
		@NonNull Long userId,
		@NonNull BigDecimal amount,
		@NonNull String orderId,
		@NonNull String reason
	);

	void refundPoint(
		@NonNull Long userId,
		@NonNull BigDecimal amount,
		@NonNull String orderId,
		@NonNull String reason
	);

	BigDecimal getAvailablePoint(Long userId);
}
