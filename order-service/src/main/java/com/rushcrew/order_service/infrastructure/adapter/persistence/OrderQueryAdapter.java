package com.rushcrew.order_service.infrastructure.adapter.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rushcrew.order_service.application.query.dto.OrderDetailDto;
import com.rushcrew.order_service.application.query.dto.OrderItemQueryDto;
import com.rushcrew.order_service.application.query.dto.OrderListDto;
import com.rushcrew.order_service.application.query.dto.OrderSearchCriteria;
import com.rushcrew.order_service.application.query.port.out.OrderQueryPort;
import com.rushcrew.order_service.domain.model.order.QOrder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderQueryAdapter implements OrderQueryPort {

	private final JPAQueryFactory queryFactory;
	private final EntityManager entityManager;

	@Override
	public Optional<OrderDetailDto> findOrderDetail(UUID orderId) {
		QOrder order = QOrder.order;

		// 1. Order 정보 조회 (QueryDSL)
		OrderDetailDto orderDetail = queryFactory
			.select(Projections.constructor(OrderDetailDto.class,
				order.orderId,
				order.userId,
				order.status.stringValue(),
				order.amount.totalAmount,
				order.amount.pointUsed,
				order.amount.finalAmount,
				order.orderedAt,
				order.paymentCompletedAt,
				order.purchaseConfirmedAt,
				order.cancelledAt,
				order.autoConfirmScheduledAt,
				order.shippingInfo
			))
			.from(order)
			.where(order.orderId.eq(orderId))
			.fetchOne();

		if (orderDetail == null) {
			return Optional.empty();
		}

		// 2. Order Items 조회
		String sql = """
			SELECT 
				oi.order_item_id,
				oi.quantity,
				oi.discount_price,
				oi.subtotal
			FROM order_schema.p_order_item oi
			WHERE oi.order_id = :orderId
			ORDER BY oi.created_at
		""";

		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("orderId", orderId);

		@SuppressWarnings("unchecked")
		List<Object[]> results = query.getResultList();

		List<OrderItemQueryDto> orderItems = results.stream()
			.map(row -> {
				try {
					OrderItemQueryDto dto = OrderItemQueryDto.builder()
						.orderItemId(row[0] instanceof UUID ? (UUID) row[0] : UUID.fromString(row[0].toString()))
						.productName(null)
						.optionName(null)
						.quantity(row[1] != null ? ((Number) row[1]).longValue() : 0L)
						.unitPrice(null)
						.discountPrice(row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO)
						.subtotal(row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO)
						.build();
					return dto;
				} catch (Exception e) {
					return null;
				}
			})
			.filter(item -> item != null)
			.toList();

		// 3. OrderItems를 포함한 완전한 DTO 반환
		OrderDetailDto finalDto = OrderDetailDto.builder()
			.orderId(orderDetail.getOrderId())
			.userId(orderDetail.getUserId())
			.orderStatus(orderDetail.getOrderStatus())
			.totalAmount(orderDetail.getTotalAmount())
			.pointUsed(orderDetail.getPointUsed())
			.finalAmount(orderDetail.getFinalAmount())
			.orderedAt(orderDetail.getOrderedAt())
			.paymentCompletedAt(orderDetail.getPaymentCompletedAt())
			.purchaseConfirmedAt(orderDetail.getPurchaseConfirmedAt())
			.cancelledAt(orderDetail.getCancelledAt())
			.autoConfirmScheduledAt(orderDetail.getAutoConfirmScheduledAt())
			.shippingInfo(orderDetail.getShippingInfo())
			.orderItems(orderItems)
			.build();

		return Optional.of(finalDto);
	}

	@Override
	public Page<OrderListDto> findByCriteria(
		OrderSearchCriteria criteria,
		Pageable pageable
	) {
		// Count 쿼리
		String countSql = """
            SELECT COUNT(DISTINCT o.order_id)
            FROM order_schema.p_order o
            WHERE o.user_id = :userId
            """;

		Query countQuery = entityManager.createNativeQuery(countSql);
		countQuery.setParameter("userId", criteria.getUserId());
		Long total = ((Number) countQuery.getSingleResult()).longValue();

		// 데이터 조회 쿼리
		String sql = """
            SELECT 
                o.order_id,
                o.status,
                o.final_amount,
                o.ordered_at,
                COUNT(oi.order_item_id)::int as item_count,
                MIN(oi.product_snapshot->>'productName') as first_product_name
            FROM order_schema.p_order o
            LEFT JOIN order_schema.p_order_item oi ON o.order_id = oi.order_id
            WHERE o.user_id = :userId
            GROUP BY o.order_id, o.status, o.final_amount, o.ordered_at
            ORDER BY o.ordered_at DESC
            LIMIT :limit OFFSET :offset
            """;

		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("userId", criteria.getUserId());
		query.setParameter("limit", pageable.getPageSize());
		query.setParameter("offset", pageable.getOffset());

		@SuppressWarnings("unchecked")
		List<Object[]> results = query.getResultList();

		List<OrderListDto> content = results.stream()
			.map(row -> OrderListDto.builder()
				.orderId(UUID.fromString(row[0].toString()))
				.orderStatus((String) row[1])
				.finalAmount((BigDecimal) row[2])
				.orderedAt(((Instant) row[3]))
				.itemCount((Integer) row[4])
				.firstProductName((String) row[5])
				.build())
			.toList();

		return new PageImpl<>(content, pageable, total);
	}
}
