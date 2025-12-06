package com.rushcrew.order_service.application.saga.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaStepResult {
	private boolean success;
	private String errorMessage;

	@Builder.Default
	private Map<String, Object> data = new HashMap<>();

	public static SagaStepResult success() {
		return SagaStepResult.builder()
			.success(true)
			.build();
	}

	public static SagaStepResult success(Map<String, Object> data) {
		return SagaStepResult.builder()
			.success(true)
			.data(data)
			.build();
	}

	public static SagaStepResult failure(String errorMessage) {
		return SagaStepResult.builder()
			.success(false)
			.errorMessage(errorMessage)
			.build();
	}
}
