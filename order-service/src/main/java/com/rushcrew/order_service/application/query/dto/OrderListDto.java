package com.rushcrew.order_service.application.query.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderListDto {
	private UUID orderId;
	private String orderStatus;
	private BigDecimal finalAmount;
	private Instant orderedAt;
	private Integer itemCount;
	private String firstProductName;
}
