package com.rushcrew.order_service.application.saga.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rushcrew.order_service.application.saga.dto.OrderCreationSagaData;
import com.rushcrew.order_service.application.saga.dto.SagaContext;
import com.rushcrew.order_service.application.saga.step.RequestStockReservationStep;
import com.rushcrew.order_service.application.saga.step.UsePointStep;
import com.rushcrew.order_service.domain.enums.SagaStatus;
import com.rushcrew.order_service.domain.enums.SagaStepName;
import com.rushcrew.order_service.domain.model.saga.SagaInstance;
import com.rushcrew.order_service.domain.model.saga.SagaStep;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Saga 보상 트랜잭션 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SagaRecoveryService {

	private final UsePointStep usePointStep;
	private final RequestStockReservationStep requestStockReservationStep;

	@Transactional
	public void compensateSaga(SagaInstance sagaInstance) {
		log.info("Saga 보상 시작: sagaId={}, type={}",
			sagaInstance.getSagaId(), sagaInstance.getSagaType());

		if (!"ORDER_CREATION".equals(sagaInstance.getSagaType())) {
			log.warn("지원하지 않는 Saga 타입: {}", sagaInstance.getSagaType());
			return;
		}

		// 완료된 Step들을 역순으로 정렬
		List<SagaStep> completedSteps = sagaInstance.getSteps().stream()
			.filter(step -> step.getStatus() == SagaStatus.COMPLETED)
			.sorted((a, b) -> b.getExecutedAt().compareTo(a.getExecutedAt()))
			.toList();

		if (completedSteps.isEmpty()) {
			log.info("보상할 완료된 Step이 없음: sagaId={}", sagaInstance.getSagaId());
			sagaInstance.fail("Saga timeout - no completed steps");
			return;
		}

		SagaContext context = SagaContext.builder()
			.sagaId(sagaInstance.getSagaId())
			.userId(sagaInstance.getUserId())
			.build();

		// SagaData 복원
		OrderCreationSagaData data = sagaInstance.restoreData();

		if (data == null) {
			log.error("SagaData 복원 실패: sagaId={}", sagaInstance.getSagaId());
			sagaInstance.fail("Saga timeout - data restoration failed");
			return;
		}

		// 역순으로 보상 실행
		int successCount = 0;
		int failCount = 0;

		for (SagaStep step : completedSteps) {
			try {
				compensateStep(step.getStepName(), context, data, sagaInstance);
				step.markAsCompensated();
				successCount++;
				log.info("Step 보상 완료: sagaId={}, step={}",
					sagaInstance.getSagaId(), step.getStepName());
			} catch (Exception e) {
				log.error("Step 보상 실패: sagaId={}, step={}",
					sagaInstance.getSagaId(), step.getStepName(), e);
				step.markAsFailed(e.getMessage());
				failCount++;
			}
		}

		sagaInstance.fail(String.format(
			"Saga timeout - compensation finished (success=%d, fail=%d)",
			successCount, failCount
		));

		log.info("Saga 보상 완료: sagaId={}, success={}, fail={}",
			sagaInstance.getSagaId(), successCount, failCount);
	}

	/**
	 * Step별 보상 로직 실행
	 */
	private void compensateStep(
		String stepName,
		SagaContext context,
		OrderCreationSagaData data,
		SagaInstance sagaInstance
	) {
		log.info("Step 보상 실행: sagaId={}, step={}", context.getSagaId(), stepName);

		switch (stepName) {
			case "USE_POINT":
				usePointStep.compensate(context, data);
				sagaInstance.addStep(SagaStepName.USE_POINT_COMPENSATE, SagaStatus.COMPLETED);
				break;

			case "REQUEST_STOCK_RESERVATION":
				log.info("[Saga-{}] 재고 예약 취소 실행", context.getSagaId());
				requestStockReservationStep.compensate(context, data);
				sagaInstance.addStep(
					SagaStepName.REQUEST_STOCK_RESERVATION_COMPENSATE,
					SagaStatus.COMPLETED
				);
				break;

			case "VALIDATE_STOCK":
				log.info("[Saga-{}] 보상 불필요한 Step: VALIDATE_STOCK", context.getSagaId());
				break;

			case "CREATE_ORDER":
				log.info("[Saga-{}] 보상 불필요한 Step: {}", context.getSagaId(), stepName);
				break;

			default:
				log.warn("[Saga-{}] 알 수 없는 Step: {}", context.getSagaId(), stepName);
		}
	}
}
