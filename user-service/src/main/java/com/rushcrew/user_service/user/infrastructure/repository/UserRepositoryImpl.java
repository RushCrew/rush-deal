package com.rushcrew.user_service.user.infrastructure.repository;

import com.rushcrew.common.exception.BusinessException;
import com.rushcrew.user_service.user.domain.entity.User;
import com.rushcrew.user_service.user.domain.error.UserErrorCode;
import com.rushcrew.user_service.user.domain.repository.UserRepository;
import java.util.List;
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
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    @Override
    public User getById(Long id) {
        return userJpaRepository.findById(id)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    @Override
    public List<User> getAll() {
        return userJpaRepository.findAll();
    }
}
