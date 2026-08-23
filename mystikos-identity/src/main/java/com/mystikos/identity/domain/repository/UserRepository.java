package com.mystikos.identity.domain.repository;

import com.mystikos.common.result.PageResult;
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

    /** 按创建时间倒序分页查询用户列表，运营态用户管理用。 */
    PageResult<User> findPage(int pageNum, int pageSize);
}
