package com.mystikos.identity.application.service;

import com.mystikos.identity.domain.model.Gender;
import com.mystikos.identity.domain.model.Role;
import com.mystikos.identity.domain.model.UserStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record UserProfileView(
        Long userId,
        String phone,
        String email,
        String nickname,
        boolean privacyAnonymous,
        Gender gender,
        String avatarUrl,
        LocalDate birthDate,
        String bio,
        String regionCode,
        List<TagView> tags,
        Set<Role> roles,
        Integer membershipTierLevel,
        String membershipTierCode,
        UserStatus status
) {
}
