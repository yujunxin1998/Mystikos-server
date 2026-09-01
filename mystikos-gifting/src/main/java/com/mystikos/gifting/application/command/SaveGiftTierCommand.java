package com.mystikos.gifting.application.command;

import java.math.BigDecimal;

public record SaveGiftTierCommand(
        Long id,
        String code,
        String displayName,
        String displayNameEn,
        BigDecimal multiplier,
        int sortOrder,
        boolean active
) {
}
