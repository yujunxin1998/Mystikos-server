package com.mystikos.identity.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/** 打手列表的联表只读投影（identity_user + identity_companion_profile），供后台管理列表展示用。 */
public record CompanionSummary(
        Long userId,
        String phone,
        String email,
        String nickname,
        String avatarUrl,
        CompanionStatus status,
        String level,
        List<String> skillTags,
        BigDecimal hourlyRate,
        String idCardNo,
        String bankAccountName,
        String bankAccountNo,
        String bankName,
        OffsetDateTime createdAt
) {
}
