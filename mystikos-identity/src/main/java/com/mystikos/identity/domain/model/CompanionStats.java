package com.mystikos.identity.domain.model;

import java.math.BigDecimal;

/** 打手统计卡片：总打手数/可用/忙碌/平均时薪，不受列表搜索条件影响，全量统计。 */
public record CompanionStats(
        long totalCount,
        long availableCount,
        long busyCount,
        BigDecimal avgHourlyRate
) {
}
