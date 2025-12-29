package com.rushcrew.order_service.infrastructure.dto.payment;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCancelRequest {
	private UUID orderId;
	private Long userId;
}
