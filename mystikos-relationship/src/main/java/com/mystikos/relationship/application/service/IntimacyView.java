package com.mystikos.relationship.application.service;

import java.math.BigDecimal;

public record IntimacyView(Long patronId, Long companionId, int stage, BigDecimal progressValue) {

    public static IntimacyView empty(Long patronId, Long companionId) {
        return new IntimacyView(patronId, companionId, 0, BigDecimal.ZERO);
    }
}
