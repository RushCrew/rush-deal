package com.rushcrew.order_service.domain.model.order;

import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.Type;

import com.rushcrew.order_service.domain.common.BaseEntity;
import com.rushcrew.order_service.domain.enums.OrderEventType;
import com.rushcrew.order_service.domain.enums.OrderStatus;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_order_history", schema = "order_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OrderHistory extends BaseEntity {

	@Id
	private UUID orderHistoryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private OrderEventType eventType;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private OrderStatus previousStatus;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OrderStatus newStatus;

	@Column(columnDefinition = "TEXT")
	private String reason;

	@Type(JsonBinaryType.class)
	@Column(columnDefinition = "jsonb")
	private Map<String, Object> metadata;

	// ============================================
	//                 도메인 로직
	// ============================================

	public static OrderHistory create(
		Order order,
		OrderEventType eventType,
		OrderStatus previousStatus,
		OrderStatus newStatus,
		String reason
	) {
		if (order == null) {
			throw new IllegalArgumentException("주문 정보는 필수입니다.");
		}
		if (eventType == null) {
			throw new IllegalArgumentException("이벤트 타입은 필수입니다.");
		}
		if (newStatus == null) {
			throw new IllegalArgumentException("새 상태는 필수입니다.");
		}

		return OrderHistory.builder()
			.orderHistoryId(UUID.randomUUID())
			.order(order)
			.eventType(eventType)
			.previousStatus(previousStatus)
			.newStatus(newStatus)
			.reason(reason)
			.build();
	}

	// 메타데이터를 포함하는 주문 이력 생성
	public static OrderHistory createWithMetadata(
		Order order,
		OrderEventType eventType,
		OrderStatus previousStatus,
		OrderStatus newStatus,
		String reason,
		Map<String, Object> metadata
	) {
		OrderHistory history = create(order, eventType, previousStatus, newStatus, reason);
		history.metadata = metadata;
		return history;
	}

}
