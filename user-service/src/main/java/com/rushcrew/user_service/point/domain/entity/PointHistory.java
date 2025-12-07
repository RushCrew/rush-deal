package com.rushcrew.user_service.point.domain.entity;

import com.rushcrew.user_service.point.domain.enums.PointStatus;
import com.rushcrew.user_service.point.domain.enums.PointType;
import com.rushcrew.user_service.point.domain.vo.OrderId;
import com.rushcrew.user_service.point.domain.vo.Point;
import com.rushcrew.user_service.point.domain.vo.PointWalletId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_point_history", schema = "user_schema")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "point_wallet_id"))
    private PointWalletId pointWalletId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "order_id"))
    private OrderId orderId;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "amount"))
    private Point amount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "balance_after"))
    private Point balanceAfter;

    @Enumerated(EnumType.STRING)
    private PointType type;

    @Enumerated(EnumType.STRING)
    private PointStatus status;

    private LocalDateTime expiresAt;

    public static PointHistory create(
        PointWalletId pointWalletId,
        OrderId orderId,
        Point amount,
        Point balanceAfter,
        PointType type,
        PointStatus status,
        LocalDateTime expiresAt
    ) {
        PointHistory history = new PointHistory();

        history.pointWalletId = pointWalletId;
        history.orderId = orderId;
        history.amount = amount;
        history.balanceAfter = balanceAfter;
        history.type = type;
        history.status = status;
        history.expiresAt = expiresAt;

        return history;
    }

}
