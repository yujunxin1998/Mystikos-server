package com.mystikos.identity.application.service;

import com.mystikos.identity.domain.model.Role;
import com.mystikos.identity.domain.model.UserStatus;

import java.util.Set;

public record UserProfileView(
        Long userId,
        String phone,
        String email,
        String nickname,
        boolean privacyAnonymous,
        Set<Role> roles,
        Integer membershipTierLevel,
        String membershipTierCode,
        UserStatus status
) {
}
