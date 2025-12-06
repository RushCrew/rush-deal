package com.rushcrew.order_service.presentation.api.command;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

import com.rushcrew.common.dto.ApiResponse;
import com.rushcrew.order_service.application.command.dto.command.CreateOrderCommand;
import com.rushcrew.order_service.application.command.dto.result.CreateOrderResult;
import com.rushcrew.order_service.application.command.usecase.CreateOrderUseCase;
import com.rushcrew.order_service.global.util.RoleChecker;
import com.rushcrew.order_service.presentation.dto.request.CreateOrderRequest;
import com.rushcrew.order_service.presentation.dto.response.CreateOrderResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Order Command", description = "주문 생성/수정/취소 API")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderCommandController {

	private final CreateOrderUseCase createOrderUseCase;

	@Operation(summary = "주문 생성")
	@PostMapping
	public ApiResponse<CreateOrderResponse> createOrder(
		@Valid @RequestBody CreateOrderRequest request,
		@RequestHeader("X-User-Id") Long userId,
		@RequestHeader(value = "X-User-Role", required = false) String role
	) {
		RoleChecker.checkRole(role, "USER", "MASTER", "SELLER");

		CreateOrderCommand command = CreateOrderCommand.builder()
			.userId(userId)
			.timeDealId(UUID.fromString(request.timeDealId()))
			.orderItems(request.orderItems().stream()
				.map(item -> CreateOrderCommand.OrderItemCommand.builder()
					.timeDealStockId(UUID.fromString(item.timeDealStockId()))
					.quantity(item.quantity())
					.build())
				.collect(Collectors.toList()))
			.pointUsed(request.pointUsed())
			.shippingInfo(request.shippingInfo().toShippingInfo())
			.build();

		CreateOrderResult result = createOrderUseCase.createOrder(command);

		return ApiResponse.success(CreateOrderResponse.from(result));
	}

}
