package com.mystikos.leaderboard.domain.model;

import java.math.BigDecimal;

/**
 * 陪玩魅力值累计统计。榜单排名不落库，查询时按 charmValue 实时排序
 * （见需求讨论：先做实时计算，"每周一更新"的冻结快照留到后续需要时再加调度）。
 */
public class CompanionCharmStat {

    private final Long companionId;
    private BigDecimal charmValue;

    private CompanionCharmStat(Long companionId, BigDecimal charmValue) {
        this.companionId = companionId;
        this.charmValue = charmValue;
    }

    public static CompanionCharmStat initiate(Long companionId) {
        return new CompanionCharmStat(companionId, BigDecimal.ZERO);
    }

    public static CompanionCharmStat restore(Long companionId, BigDecimal charmValue) {
        return new CompanionCharmStat(companionId, charmValue);
    }

    public void accrue(BigDecimal amount) {
        this.charmValue = this.charmValue.add(amount);
    }

    public Long getCompanionId() {
        return companionId;
    }

    public BigDecimal getCharmValue() {
        return charmValue;
    }
}
