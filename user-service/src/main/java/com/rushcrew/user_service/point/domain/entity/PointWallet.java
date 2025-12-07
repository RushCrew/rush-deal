package com.rushcrew.user_service.point.domain.entity;

import com.rushcrew.user_service.point.domain.vo.Point;
import com.rushcrew.user_service.user.domain.entity.User;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_point_wallet", schema = "user_schema")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointWallet {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "balance"))
    private Point balance;

    @Version
    private Long version;

    public static PointWallet create(User user) {
        PointWallet wallet = new PointWallet();
        wallet.id = user.getUserId();
        wallet.user = user;
        wallet.balance = Point.of(0L);
        return wallet;
    }
}
