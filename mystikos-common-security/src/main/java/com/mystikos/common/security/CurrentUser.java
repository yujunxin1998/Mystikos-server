package com.mystikos.common.security;

import java.util.Set;

/**
 * 已认证用户的最小视图，从 SecurityContext 中的 Authentication 解析得到。
 */
public record CurrentUser(String userId, Set<String> roles) {

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
