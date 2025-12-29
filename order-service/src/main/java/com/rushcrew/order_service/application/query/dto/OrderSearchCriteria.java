package com.rushcrew.order_service.application.query.dto;

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
public class OrderSearchCriteria {
	private Long userId;
	private String status;
	private Instant startDate;
	private Instant endDate;
	private UUID orderId;
}
