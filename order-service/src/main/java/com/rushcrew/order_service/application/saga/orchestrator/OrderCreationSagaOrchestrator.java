package com.rushcrew.order_service.application.saga.orchestrator;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.rushcrew.order_service.application.command.dto.command.CreateOrderCommand;
import com.rushcrew.order_service.application.port.out.SagaInstancePort;
import com.rushcrew.order_service.application.saga.dto.OrderCreationSagaData;
import com.rushcrew.order_service.application.saga.dto.SagaContext;
import com.rushcrew.order_service.application.saga.step.RequestStockReservationStep;
import com.rushcrew.order_service.application.saga.step.UsePointStep;
import com.rushcrew.order_service.application.saga.step.ValidateStockStep;
import com.rushcrew.order_service.domain.enums.SagaStatus;
import com.rushcrew.order_service.domain.enums.SagaStepName;
import com.rushcrew.order_service.domain.model.saga.SagaInstance;
import com.rushcrew.order_service.application.saga.dto.SagaStepResult;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreationSagaOrchestrator {

	private final ValidateStockStep validateStockStep;
	private final UsePointStep usePointStep;
	private final RequestStockReservationStep requestStockReservationStep;
	private final SagaInstancePort sagaInstancePort;

	@Transactional
	public UUID execute(CreateOrderCommand command) {
		// 1. Saga 생성
		SagaInstance saga = SagaInstance.create(SagaStepName.CREATE_ORDER.name(), command.userId());
		sagaInstancePort.save(saga);

		// 2. Context / Data 구성
		SagaContext context = SagaContext.builder()
			.sagaId(saga.getSagaId())
			.userId(command.userId())
			.build();

		// 3. 주문 ID 미리 생성
		UUID preGeneratedOrderId = UUID.randomUUID();

		OrderCreationSagaData data = OrderCreationSagaData.builder()
			.command(command)
			.orderId(preGeneratedOrderId) // 미리 생성된 주문 ID 설정
			.queueToken(command.queueToken())
			.build();

		log.info("[Saga-{}] 주문 ID 사전 생성: orderId={}", saga.getSagaId(), preGeneratedOrderId);

		try {
			// Step 1: ValidateStock
			log.info("[Saga-{}] Step 1: ValidateStock 시작", saga.getSagaId());
			SagaStepResult validateResult = validateStockStep.execute(context, data);
			if (validateResult.isFailure()) {
				saga.fail(validateResult.getErrorMessage());
				sagaInstancePort.save(saga);
				throw new IllegalArgumentException(validateResult.getErrorMessage());
			}
			saga.addStep(SagaStepName.VALIDATE_STOCK, SagaStatus.COMPLETED);

			// Step 2: UsePoint
			log.info("[Saga-{}] Step 2: UsePoint 시작", saga.getSagaId());
			SagaStepResult pointResult = usePointStep.execute(context, data);
			if (pointResult.isFailure()) {
				// 포인트 사용 실패는 보상 불필요 (아직 차감 안 됨)
				saga.fail(pointResult.getErrorMessage());
				sagaInstancePort.save(saga);
				throw new IllegalArgumentException(pointResult.getErrorMessage());
			}
			saga.addStep(SagaStepName.USE_POINT, SagaStatus.COMPLETED);

			// Step 3: RequestStockReservation
			log.info("[Saga-{}] Step 3: RequestStockReservation 시작", saga.getSagaId());
			requestStockReservationStep.execute(context, data);
			saga.addStep(SagaStepName.REQUEST_STOCK_RESERVATION, SagaStatus.WAITING);

			// Step 완료 후 SagaData 저장
			saga.saveData(data);

			// Saga 저장
			sagaInstancePort.save(saga);

			log.info("[Saga-{}] ORDER_CREATION Saga 시작 완료 (비동기 대기)", saga.getSagaId());

			return saga.getSagaId();
		} catch (Exception e) {
			log.error("[Saga-{}] Saga 초기화 실패: {}", saga.getSagaId(), e.getMessage(), e);

			// 보상 트랜잭션: 완료된 Step만 보상
			compensateCompletedSteps(saga, context, data);
			// 보상 트랜잭션: 포인트 복구
			// if (saga.hasCompletedStep(SagaStepName.USE_POINT)) {
			// 	try {
			// 		usePointStep.compensate(context, data);
			// 	} catch (Exception compensateError) {
			// 		log.error("[Saga-{}] 포인트 보상 실패", saga.getSagaId(), compensateError);
			// 		// 보상 실패는 별도 처리 필요 (Dead Letter Queue 등)
			// 	}
			// }
			throw e;
		}
	}

	/**
	 * 완료된 Step들에 대한 보상 트랜잭션 실행
	 */
	private void compensateCompletedSteps(SagaInstance saga, SagaContext context, OrderCreationSagaData data) {
		log.info("[Saga-{}] 보상 트랜잭션 시작 (Orchestrator)", saga.getSagaId());

		// USE_POINT가 완료되었다면 보상
		if (saga.hasCompletedStep(SagaStepName.USE_POINT)) {
			try {
				log.info("[Saga-{}] 포인트 보상 실행", saga.getSagaId());
				usePointStep.compensate(context, data);
				saga.addStep(SagaStepName.USE_POINT_COMPENSATE, SagaStatus.COMPLETED);
				log.info("[Saga-{}] 포인트 보상 완료", saga.getSagaId());
			} catch (Exception compensateError) {
				log.error("[Saga-{}] 포인트 보상 실패: {}",
					saga.getSagaId(), compensateError.getMessage(), compensateError);
				// 보상 실패는 별도 처리 필요 (Dead Letter Queue, 알림 등)
			}
		}

		// VALIDATE_STOCK은 보상 불필요 (조회만 했으므로)
		log.info("[Saga-{}] 보상 트랜잭션 완료 (Orchestrator)", saga.getSagaId());
	}

}
