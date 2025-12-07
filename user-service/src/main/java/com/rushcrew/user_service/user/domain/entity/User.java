package com.rushcrew.user_service.user.domain.entity;

import com.rushcrew.user_service.point.domain.entity.PointWallet;
import com.rushcrew.user_service.user.domain.enums.UserRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_user", schema = "user_schema")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private PointWallet pointWallet;

    public static User create(String email, String password, String name, UserRole role) {
        User user = new User();

        validateUserInfo(email, password, name);

        user.email = email;
        user.password = password;
        user.name = name;
        user.role = role;

        user.pointWallet = PointWallet.create(user);
        return user;
    }

    public static void validateUserInfo(String email, String password, String name) {
        if (email == null) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        if (password == null) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }

        if (name == null) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
    }

    public void updateUser(String newPassword, String newName) {
        changePassword(newPassword);
        changeName(newName);
    }

    private void changePassword(String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다");
        }

        this.password = newPassword;
    }

    private void changeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다");
        }
        this.name = name;
    }
}
