package com.rushcrew.user_service.user.infrastructure.repository;

import com.rushcrew.user_service.user.domain.entity.User;
import com.rushcrew.user_service.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public User getByEmail(String email) {
        return userJpaRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
    }

    @Override
    public User getById(Long id) {
        return userJpaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당하는 유저가 존재하지 않습니다"));
    }
}
