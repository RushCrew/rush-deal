package com.rushcrew.order_service.domain.model.order;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.rushcrew.order_service.domain.common.BaseEntity;
import com.rushcrew.order_service.domain.enums.ReservationStatus;

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
@Table(name = "p_order_reservation", schema = "order_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OrderReservation extends BaseEntity {

	@Id
	private UUID orderReservationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Column(name = "time_deal_stock_id", nullable = false)
	private UUID timeDealStockId;

	@Column(nullable = false)
	private Long quantity;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ReservationStatus status;

	@Column(nullable = false)
	private Instant reservedAt;

	@Column(nullable = false)
	private Instant expiresAt;

	private Instant confirmedAt;

	private Instant releasedAt;


	// ============================================
	//                 도메인 로직
	// ============================================

	// 예약 생성 (15분 TTL)
	public static OrderReservation create(UUID timeDealStockId, Long quantity) {
		return OrderReservation.builder()
			.orderReservationId(UUID.randomUUID())
			.timeDealStockId(timeDealStockId)
			.quantity(quantity)
			.status(ReservationStatus.RESERVED)
			.reservedAt(Instant.now())
			.expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES)) // 15분 TTL
			.build();
	}

	// 결제 완료 시 예약 확정
	public void confirm() {
		if (this.status != ReservationStatus.RESERVED) {
			throw new IllegalStateException("RESERVED 상태에서만 확정할 수 있습니다.");
		}
		this.status = ReservationStatus.CONFIRMED;
		this.confirmedAt = Instant.now();
	}

	// 15분 경과 시 예약 만료
	public void expire() {
		if (this.status != ReservationStatus.RESERVED) {
			throw new IllegalStateException("RESERVED 상태에서만 만료 처리할 수 있습니다.");
		}
		this.status = ReservationStatus.EXPIRED;
		this.releasedAt = Instant.now();
	}

	// 주문 취소 시 예약 취소
	public void cancel() {
		if (this.status != ReservationStatus.RESERVED) {
			throw new IllegalStateException("RESERVED 상태에서만 취소할 수 있습니다.");
		}
		this.status = ReservationStatus.CANCELLED;
		this.releasedAt = Instant.now();
	}

	// 예약 만료 여부 확인
	public boolean isExpired() {
		return Instant.now().isAfter(expiresAt);
	}

	// 주문 연관 관계 설정
	public void assignOrder(Order order) {
		this.order = order;
	}

}
