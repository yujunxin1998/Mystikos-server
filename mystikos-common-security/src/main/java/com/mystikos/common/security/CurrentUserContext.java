package com.mystikos.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 从 Spring SecurityContext 读取当前用户。只依赖 Authentication 的通用契约
 * （name + authorities），具体是 JWT 还是其他认证方式由 Resource Server 接入
 * 方式决定，那部分落地后这里不需要改动。
 */
public final class CurrentUserContext {

    private CurrentUserContext() {
    }

    public static CurrentUser get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("当前请求未认证，无法获取当前用户");
        }
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return new CurrentUser(authentication.getName(), roles);
    }
}
