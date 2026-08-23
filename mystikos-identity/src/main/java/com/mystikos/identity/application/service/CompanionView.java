package com.mystikos.identity.application.service;

import com.mystikos.identity.domain.model.CompanionStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record CompanionView(
        Long userId,
        String phone,
        String email,
        String nickname,
        String avatarUrl,
        CompanionStatus status,
        String level,
        List<TagView> tags,
        BigDecimal hourlyRate,
        String idCardNo,
        String bankAccountName,
        String bankAccountNo,
        String bankName,
        OffsetDateTime createdAt,
        CompanionPerformanceView performance
) {
}
