package com.mystikos.identity.application.service;

import java.math.BigDecimal;

public record CompanionStatsView(
        long totalCount,
        long availableCount,
        long busyCount,
        BigDecimal avgHourlyRate
) {
}
