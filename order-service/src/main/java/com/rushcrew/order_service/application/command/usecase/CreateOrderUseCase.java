package com.rushcrew.order_service.application.command.usecase;

import com.rushcrew.order_service.application.command.dto.command.CreateOrderCommand;
import com.rushcrew.order_service.application.command.dto.result.CreateOrderResult;

public interface CreateOrderUseCase {
	CreateOrderResult createOrder(CreateOrderCommand command);
}
