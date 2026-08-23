package com.mystikos.identity.domain.repository;

import com.mystikos.common.result.PageResult;
import com.mystikos.identity.domain.model.User;
import com.mystikos.identity.domain.model.UserStatus;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);

    Optional<User> findByOAuthBinding(String provider, String providerUserId);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    /**
     * 按创建时间倒序分页查询用户列表，运营态用户管理用。status/createdFrom/createdTo/keyword
     * 均为可选过滤条件，为 null（或空字符串）时不参与过滤；keyword 匹配昵称/手机号/邮箱。
     */
    PageResult<User> findPage(int pageNum, int pageSize, UserStatus status,
                               OffsetDateTime createdFrom, OffsetDateTime createdTo, String keyword);
}
