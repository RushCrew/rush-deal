package com.rushcrew.order_service.application.saga.step;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rushcrew.order_service.application.command.dto.command.CreateOrderCommand;
import com.rushcrew.order_service.application.command.port.out.OrderCommandPort;
import com.rushcrew.order_service.application.port.dto.TimeDealInfo;
import com.rushcrew.order_service.application.port.out.OutboxPort;
import com.rushcrew.order_service.application.saga.dto.OrderCreationSagaData;
import com.rushcrew.order_service.application.saga.dto.SagaContext;
import com.rushcrew.order_service.application.saga.dto.SagaStepResult;
import com.rushcrew.order_service.domain.model.order.Order;
import com.rushcrew.order_service.domain.model.order.OrderItem;
import com.rushcrew.order_service.domain.model.order.OrderReservation;
import com.rushcrew.order_service.domain.vo.ProductSnapshot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateOrderStep {

	private final OrderCommandPort orderCommandPort;
	private final OutboxPort outboxPort;
	private final ObjectMapper objectMapper;

	/**
	 * Forward Transaction: 주문 생성
	 * - Order 엔티티 생성 및 저장
	 * - Outbox에 ORDER_CREATED 이벤트 저장
	 */
	public SagaStepResult execute(SagaContext context, OrderCreationSagaData data) {
		try {
			log.info("[Saga-{}] CreateOrder 실행 시작", context.getSagaId());

			CreateOrderCommand command = data.getCommand();
			TimeDealInfo timeDeal = (TimeDealInfo) context.getData("timeDeal");

			// 1. OrderItem 목록 생성
			List<OrderItem> orderItems = data.getOrderItems().stream()
				.map(itemResult -> {
					// ProductSnapshot 생성
					ProductSnapshot snapshot = ProductSnapshot.builder()
						.timeDealStockId(itemResult.timeDealStockId())
						.productId(itemResult.productId())
						.productName(itemResult.productName())
						.productDescription("") // stockDetail에서 가져와야 함
						.optionName(itemResult.optionName())
						.timeDealId(timeDeal.timeDealId())
						.timeDealTitle(timeDeal.title())
						.discountRate(timeDeal.discountRate())
						.build();

					return OrderItem.create(
						itemResult.timeDealStockId(),
						itemResult.quantity(),
						itemResult.unitPrice(),
						itemResult.discountPrice(),
						snapshot
					);
				})
				.collect(Collectors.toList());

			// 2. Order 생성
			Order order = Order.create(
				command.userId(),
				orderItems,
				command.pointUsed(),
				command.shippingInfo()
			);

			// 3. OrderReservation 추가
			for (CreateOrderCommand.OrderItemCommand itemCommand : command.orderItems()) {
				OrderReservation reservation = OrderReservation.create(
					itemCommand.timeDealStockId(),
					itemCommand.quantity()
				);
				order.addReservation(reservation);
			}

			// 4. Order 저장 (PostgreSQL)
			Order savedOrder = orderCommandPort.save(order);

			log.info("[Saga-{}] Order 저장 완료: orderId={}", context.getSagaId(), savedOrder.getOrderId());

			// 5. Outbox에 ORDER_CREATED 이벤트 저장 (같은 트랜잭션!)
			Map<String, Object> eventPayload = new HashMap<>();
			eventPayload.put("orderId", savedOrder.getOrderId());
			eventPayload.put("userId", savedOrder.getUserId());
			eventPayload.put("status", savedOrder.getStatus().name());
			eventPayload.put("totalAmount", savedOrder.getTotalAmount());
			eventPayload.put("pointUsed", savedOrder.getPointUsed());
			eventPayload.put("finalAmount", savedOrder.getFinalAmount());
			eventPayload.put("orderedAt", savedOrder.getOrderedAt());

			outboxPort.createAndSave(
				"ORDER",
				savedOrder.getOrderId(),
				"ORDER_CREATED",
				objectMapper.writeValueAsString(eventPayload)
			);

			log.info("[Saga-{}] Outbox 이벤트 저장 완료", context.getSagaId());

			// 6. SagaData에 orderId 저장
			data.setOrderId(savedOrder.getOrderId());
			context.setData("orderId", savedOrder.getOrderId());

			// 7. 결과 데이터 생성
			Instant reservationExpiresAt = savedOrder.getReservations().stream()
				.map(OrderReservation::getExpiresAt)
				.findFirst()
				.orElse(Instant.now().plus(15, ChronoUnit.MINUTES));

			// reservationExpiresAt, orderedAt, orderStatus를 context에 저장 (OrderCreationSagaOrchestrator에서 사용)
			context.setData("reservationExpiresAt", reservationExpiresAt);
			context.setData("orderedAt", savedOrder.getOrderedAt());
			context.setData("orderStatus", savedOrder.getStatus().name());

			Map<String, Object> resultData = new HashMap<>();
			resultData.put("orderId", savedOrder.getOrderId());
			resultData.put("reservationExpiresAt", reservationExpiresAt);

			log.info("[Saga-{}] CreateOrder 실행 완료: orderId={}",
				context.getSagaId(), savedOrder.getOrderId());

			return SagaStepResult.success(resultData);

		} catch (JsonProcessingException e) {
			log.error("[Saga-{}] CreateOrder 실패: JSON 변환 오류", context.getSagaId(), e);
			return SagaStepResult.failure("주문 생성 실패: JSON 변환 오류");
		} catch (Exception e) {
			log.error("[Saga-{}] CreateOrder 실패", context.getSagaId(), e);
			return SagaStepResult.failure("주문 생성 실패: " + e.getMessage());
		}
	}

	/**
	 * Compensating Transaction: 주문 취소
	 */
	public void compensate(SagaContext context, OrderCreationSagaData data) {
		try {
			log.info("[Saga-{}] CreateOrder 보상 트랜잭션 시작", context.getSagaId());

			UUID orderId = data.getOrderId();
			if (orderId == null) {
				log.warn("[Saga-{}] orderId가 없어서 보상 트랜잭션 스킵", context.getSagaId());
				return;
			}

			// 주문 취소
			Order order = orderCommandPort.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

			order.cancelBeforePayment("Saga 실패로 인한 자동 취소");
			orderCommandPort.save(order);

			// Outbox에 ORDER_CANCELLED 이벤트 저장
			try {
				Map<String, Object> eventPayload = new HashMap<>();
				eventPayload.put("orderId", orderId);
				eventPayload.put("reason", "Saga 실패로 인한 자동 취소");
				eventPayload.put("cancelledAt", Instant.now());

				outboxPort.createAndSave(
					"ORDER",
					orderId,
					"ORDER_CANCELLED",
					objectMapper.writeValueAsString(eventPayload)
				);
			} catch (JsonProcessingException e) {
				log.error("[Saga-{}] ORDER_CANCELLED 이벤트 저장 실패", context.getSagaId(), e);
			}

			log.info("[Saga-{}] CreateOrder 보상 트랜잭션 완료: orderId={}",
				context.getSagaId(), orderId);

		} catch (Exception e) {
			log.error("[Saga-{}] CreateOrder 보상 트랜잭션 실패", context.getSagaId(), e);
			// 보상 트랜잭션 실패는 별도 알림/모니터링 필요
		}
	}
}
