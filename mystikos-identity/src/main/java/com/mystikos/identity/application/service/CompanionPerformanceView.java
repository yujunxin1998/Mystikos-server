package com.mystikos.identity.application.service;

import java.math.BigDecimal;

/**
 * 打手业绩统计（接单数/流水/评分），占位实现——还没接 Booking（完单事件）/Leaderboard
 * （魅力值）数据源，先固定返回空值，字段结构先定下来方便前端对接。
 */
public record CompanionPerformanceView(
        long totalOrders,
        BigDecimal totalRevenue,
        BigDecimal avgRating
) {
    public static CompanionPerformanceView placeholder() {
        return new CompanionPerformanceView(0, BigDecimal.ZERO, null);
    }
}
