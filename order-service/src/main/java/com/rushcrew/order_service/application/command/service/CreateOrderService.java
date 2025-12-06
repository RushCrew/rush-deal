package com.rushcrew.order_service.application.command.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rushcrew.order_service.application.command.dto.command.CreateOrderCommand;
import com.rushcrew.order_service.application.command.dto.result.CreateOrderResult;
import com.rushcrew.order_service.application.command.port.out.OrderCachePort;
import com.rushcrew.order_service.application.command.usecase.CreateOrderUseCase;
import com.rushcrew.order_service.application.saga.orchestrator.OrderCreationSagaOrchestrator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {

	private final OrderCreationSagaOrchestrator sagaOrchestrator;
	private final OrderCachePort orderCachePort;

	@Override
	@Transactional
	public CreateOrderResult createOrder(CreateOrderCommand command) {
		log.info("주문 생성 시작: userId={}, timeDealId={}",
			command.userId(), command.timeDealId());

		// Saga 실행
		CreateOrderResult result = sagaOrchestrator.execute(command);

		// Redis 캐시 저장
		orderCachePort.saveOrderCache(result);

		log.info("주문 생성 완료: orderId={}", result.orderId());

		return result;
	}
}
