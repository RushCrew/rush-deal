package com.rushcrew.user_service.user.domain.repository;

import com.rushcrew.user_service.user.domain.entity.User;

public interface UserRepository {

    User getById(Long id);

    boolean existsByEmail(String email);

    User getByEmail(String email);

    User save(User user);
}
