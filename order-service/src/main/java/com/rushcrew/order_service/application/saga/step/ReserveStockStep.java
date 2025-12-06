package com.rushcrew.order_service.application.saga.step;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.rushcrew.common.exception.BusinessException;
import com.rushcrew.order_service.application.command.dto.command.CreateOrderCommand;
import com.rushcrew.order_service.application.command.dto.result.CreateOrderResult;
import com.rushcrew.order_service.application.port.dto.StockReservationResult;
import com.rushcrew.order_service.application.port.dto.TimeDealInfo;
import com.rushcrew.order_service.application.port.dto.TimeDealStockDetail;
import com.rushcrew.order_service.application.port.out.OrderEventPort;
import com.rushcrew.order_service.application.port.out.TimeDealStockPort;
import com.rushcrew.order_service.application.saga.dto.OrderCreationSagaData;
import com.rushcrew.order_service.application.saga.dto.SagaContext;
import com.rushcrew.order_service.application.saga.dto.SagaStepResult;
import com.rushcrew.order_service.application.validator.TimeDealStockValidator;
import com.rushcrew.order_service.global.error.OrderErrorCode;
import com.rushcrew.order_service.infrastructure.adapter.out.lock.DistributedLockManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReserveStockStep {

	private final TimeDealStockPort timeDealStockPort;
	private final OrderEventPort orderEventPort;
	private final DistributedLockManager lockManager;
	private final TimeDealStockValidator timeDealStockValidator;

	/**
	 * Forward Transaction: 재고 예약
	 * - 각 상품에 대해 재고 예약 요청
	 * - 분산 락을 사용하여 동시성 제어
	 */
	public SagaStepResult execute(SagaContext context, OrderCreationSagaData data) {
		try {
			log.info("[Saga-{}] ReserveStock 실행 시작", context.getSagaId());

			CreateOrderCommand command = data.getCommand();
			TimeDealInfo timeDeal = (TimeDealInfo) context.getData("timeDeal");

			List<CreateOrderResult.OrderItemResult> orderItems = new ArrayList<>();
			List<ReservedStockInfo> reservedStocks = new ArrayList<>();

			for (CreateOrderCommand.OrderItemCommand itemCommand : command.orderItems()) {
				UUID timeDealStockId = itemCommand.timeDealStockId();
				String lockKey = "stock:" + timeDealStockId + ":lock";

				try {
					// 분산 락으로 재고 예약
					ReservedStockInfo reservedStock = lockManager.executeWithLock(
						lockKey,
						5, // 5초 대기
						3, // 3초 TTL
						TimeUnit.SECONDS,
						() -> reserveStockWithLock(
							context,
							itemCommand,
							timeDeal,
							command.userId(),
							command.timeDealId()
						)
					);

					reservedStocks.add(reservedStock);

					// OrderItemResult 생성
					CreateOrderResult.OrderItemResult itemResult = CreateOrderResult.OrderItemResult.builder()
						.orderItemId(UUID.randomUUID())
						.timeDealStockId(itemCommand.timeDealStockId())
						.timeDealId(reservedStock.getStockDetail().getTimeDealId())
						.productId(reservedStock.getStockDetail().getProductId())
						.productName(reservedStock.getStockDetail().getProductName())
						.optionName(reservedStock.getStockDetail().getOptionName())
						.quantity(itemCommand.quantity())
						.unitPrice(reservedStock.getStockDetail().getProductPrice())
						.discountPrice(timeDeal.discountPrice())
						.subtotal(timeDeal.discountPrice().multiply(BigDecimal.valueOf(itemCommand.quantity())))
						.build();

					orderItems.add(itemResult);

				} catch (Exception e) {
					log.error("[Saga-{}] 재고 예약 실패: timeDealStockId={}",
						context.getSagaId(), timeDealStockId, e);

					// 이미 예약된 재고들 롤백
					rollbackReservedStocks(context, reservedStocks, command);

					return SagaStepResult.failure("재고 예약 실패: " + e.getMessage());
				}
			}

			// SagaData에 저장
			data.setOrderItems(orderItems);
			data.setTotalAmount(calculateTotalAmount(orderItems));
			data.setFinalAmount(data.getTotalAmount().subtract(command.pointUsed()));
			context.setData("reservedStocks", reservedStocks);

			log.info("[Saga-{}] ReserveStock 실행 완료: {} 건", context.getSagaId(), reservedStocks.size());

			Map<String, Object> resultData = new HashMap<>();
			resultData.put("reservedStocks", reservedStocks);
			resultData.put("orderItems", orderItems);

			return SagaStepResult.success(resultData);

		} catch (Exception e) {
			log.error("[Saga-{}] ReserveStock 실행 중 예외 발생", context.getSagaId(), e);
			return SagaStepResult.failure("재고 예약 중 오류 발생: " + e.getMessage());
		}
	}

	/**
	 * Compensating Transaction: 재고 복구
	 */
	public void compensate(SagaContext context, OrderCreationSagaData data) {
		try {
			log.info("[Saga-{}] ReserveStock 보상 트랜잭션 시작", context.getSagaId());

			@SuppressWarnings("unchecked")
			List<ReservedStockInfo> reservedStocks =
				(List<ReservedStockInfo>) context.getData("reservedStocks");

			if (reservedStocks == null || reservedStocks.isEmpty()) {
				log.warn("[Saga-{}] 복구할 재고 예약 정보가 없습니다", context.getSagaId());
				return;
			}

			rollbackReservedStocks(context, reservedStocks, data.getCommand());

			log.info("[Saga-{}] ReserveStock 보상 트랜잭션 완료: {} 건 복구",
				context.getSagaId(), reservedStocks.size());

		} catch (Exception e) {
			log.error("[Saga-{}] ReserveStock 보상 트랜잭션 실패", context.getSagaId(), e);
			// 보상 실패 시 알림 필요 (Slack, Email 등)
		}
	}

	private ReservedStockInfo reserveStockWithLock(
		SagaContext context,
		CreateOrderCommand.OrderItemCommand itemCommand,
		TimeDealInfo timeDeal,
		Long userId,
		UUID timeDealId
	) {
		// 1. 타임딜 재고 상세 정보 조회
		TimeDealStockDetail stockDetail = timeDealStockPort
			.getTimeDealStockDetail(itemCommand.timeDealStockId());

		// 2. 상품 활성 상태 검증
		timeDealStockValidator.validate(stockDetail);

		// 3. 재고 예약 요청
		StockReservationResult result = timeDealStockPort.reserveStock(
			itemCommand.timeDealStockId(),
			itemCommand.quantity(),
			userId
		);

		if (!result.isSuccess()) {
			// 재고 소진 이벤트 발행
			handleStockDepletion(timeDealId, userId, result.getAvailableStock());
			throw new BusinessException(OrderErrorCode.STOCK_DEPLETED);
		}

		log.debug("[Saga-{}] 재고 예약 성공: timeDealStockId={}, quantity={}",
			context.getSagaId(), itemCommand.timeDealStockId(), itemCommand.quantity());

		return ReservedStockInfo.builder()
			.timeDealStockId(itemCommand.timeDealStockId())
			.quantity(itemCommand.quantity())
			.stockDetail(stockDetail)
			.build();
	}

	private void rollbackReservedStocks(
		SagaContext context,
		List<ReservedStockInfo> reservedStocks,
		CreateOrderCommand command
	) {
		for (ReservedStockInfo stock : reservedStocks) {
			try {
				timeDealStockPort.restoreStock(
					stock.getTimeDealStockId(),
					stock.getQuantity(),
					context.getSagaId(), // orderId 대신 sagaId 사용
					"Saga 실패로 인한 재고 복구"
				);
				log.info("[Saga-{}] 재고 복구 완료: timeDealStockId={}",
					context.getSagaId(), stock.getTimeDealStockId());
			} catch (Exception e) {
				log.error("[Saga-{}] 재고 복구 실패: timeDealStockId={}",
					context.getSagaId(), stock.getTimeDealStockId(), e);
			}
		}
	}

	private void handleStockDepletion(UUID timeDealId, Long userId, Integer availableStock) {
		orderEventPort.publishStockDepletedEvent(
			timeDealId,
			userId,
			availableStock,
			java.time.Instant.now()
		);
	}

	private BigDecimal calculateTotalAmount(List<CreateOrderResult.OrderItemResult> items) {
		return items.stream()
			.map(CreateOrderResult.OrderItemResult::subtotal)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	@lombok.Builder
	@lombok.Getter
	private static class ReservedStockInfo {
		private UUID timeDealStockId;
		private Integer quantity;
		private TimeDealStockDetail stockDetail;
	}
}
