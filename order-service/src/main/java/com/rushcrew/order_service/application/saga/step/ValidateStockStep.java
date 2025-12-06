package com.rushcrew.order_service.application.saga.step;

import org.springframework.stereotype.Component;

import com.rushcrew.common.exception.BusinessException;
import com.rushcrew.order_service.application.command.dto.command.CreateOrderCommand;
import com.rushcrew.order_service.application.port.dto.TimeDealInfo;
import com.rushcrew.order_service.application.port.out.QueuePort;
import com.rushcrew.order_service.application.port.out.TimeDealStockPort;
import com.rushcrew.order_service.application.saga.dto.OrderCreationSagaData;
import com.rushcrew.order_service.application.saga.dto.SagaContext;
import com.rushcrew.order_service.application.saga.dto.SagaStepResult;
import com.rushcrew.order_service.application.validator.OrderItemValidator;
import com.rushcrew.order_service.application.validator.PurchaseLimitValidator;
import com.rushcrew.order_service.application.validator.QueueTokenValidator;
import com.rushcrew.order_service.application.validator.TimeDealValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateStockStep {

	private final TimeDealStockPort timeDealStockPort;
	private final QueuePort queuePort;
	private final QueueTokenValidator queueTokenValidator;
	private final TimeDealValidator timeDealValidator;
	private final OrderItemValidator orderItemValidator;
	private final PurchaseLimitValidator purchaseLimitValidator;

	/**
	 * Forward Transaction: 재고 검증
	 * - 대기열 토큰 검증
	 * - 타임딜 정보 조회 및 검증
	 * - 중복 상품 검증
	 * - 구매 수량 제한 검증
	 */
	public SagaStepResult execute(SagaContext context, OrderCreationSagaData data) {
		try {
			log.info("[Saga-{}] ValidateStock 실행 시작", context.getSagaId());

			CreateOrderCommand command = data.getCommand();

			// 1. 대기열 토큰 검증
			queueTokenValidator.validate(command.timeDealId(), command.userId());
			log.debug("[Saga-{}] 대기열 토큰 검증 완료", context.getSagaId());

			// 2. 타임딜 정보 조회 및 검증
			TimeDealInfo timeDeal = timeDealStockPort.getTimeDeal(command.timeDealId());
			timeDealValidator.validate(timeDeal);
			log.debug("[Saga-{}] 타임딜 검증 완료: {}", context.getSagaId(), timeDeal.title());

			// 3. 중복 상품 검증
			orderItemValidator.validate(command.orderItems());
			log.debug("[Saga-{}] 주문 아이템 검증 완료", context.getSagaId());

			// 4. 구매 수량 제한 검증
			purchaseLimitValidator.validate(
				command.userId(),
				command.timeDealId(),
				command.orderItems(),
				timeDeal
			);
			log.debug("[Saga-{}] 구매 제한 검증 완료", context.getSagaId());

			// SagaData에 TimeDealInfo 저장 (다음 Step에서 사용)
			context.setData("timeDeal", timeDeal);

			log.info("[Saga-{}] ValidateStock 실행 완료", context.getSagaId());
			return SagaStepResult.success();

		} catch (BusinessException e) {
			log.error("[Saga-{}] ValidateStock 실행 실패: {}", context.getSagaId(), e.getMessage());
			return SagaStepResult.failure(e.getMessage());
		} catch (Exception e) {
			log.error("[Saga-{}] ValidateStock 실행 중 예외 발생", context.getSagaId(), e);
			return SagaStepResult.failure("재고 검증 실패: " + e.getMessage());
		}
	}

	// 보상 트랜잭션 불필요 (조회/검증만 수행)
}
