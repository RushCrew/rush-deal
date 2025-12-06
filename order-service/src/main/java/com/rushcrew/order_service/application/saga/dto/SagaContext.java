package com.rushcrew.order_service.application.saga.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaContext {
	private UUID sagaId;
	private Long userId;

	@Builder.Default
	private Map<String, Object> data = new HashMap<>();

	public void setData(String key, Object value) {
		this.data.put(key, value);
	}

	public Object getData(String key) {
		return this.data.get(key);
	}
}
