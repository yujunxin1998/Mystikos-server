package com.mystikos.relationship.application.command;

import java.math.BigDecimal;

public record SaveIntimacyLevelCommand(
        Long id,
        String code,
        String displayNameZh,
        String displayNameEn,
        BigDecimal threshold,
        String perkDescription,
        int sortOrder
) {
}
