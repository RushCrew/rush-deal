package com.rushcrew.user_service.user.infrastructure.validator;

import com.rushcrew.common.exception.BusinessException;
import com.rushcrew.user_service.user.domain.entity.User;
import com.rushcrew.user_service.user.domain.error.UserErrorCode;
import com.rushcrew.user_service.user.domain.repository.UserRepository;
import com.rushcrew.user_service.user.domain.service.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidatorImpl implements UserValidator {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(UserErrorCode.DUPLICATE_EMAIL);
        }
    }

    @Override
    public void validatePassword(User user, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BusinessException(UserErrorCode.INVALID_PASSWORD);
        }
    }
}
