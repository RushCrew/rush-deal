package com.rushcrew.order_service.application.saga.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.rushcrew.order_service.application.command.dto.command.CreateOrderCommand;
import com.rushcrew.order_service.application.command.dto.result.CreateOrderResult;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreationSagaData {
	private CreateOrderCommand command;

	// Step 실행 결과
	private BigDecimal totalAmount;
	private BigDecimal finalAmount;
	private List<CreateOrderResult.OrderItemResult> orderItems;

	// 생성된 주문 ID
	private UUID orderId;

}
