package com.mystikos.identity.domain.repository;

import com.mystikos.identity.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);

    Optional<User> findByOAuthBinding(String provider, String providerUserId);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);
}
