package com.mystikos.relationship.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 某老板对某陪玩，某一天已经累加进亲密度的数值——防刷用的计数器，不是业务事实本身
 * （赠礼本身照常成立，只是超出当日上限的部分不再计入亲密度，见 RelationshipApplicationService）。
 */
public class IntimacyDailyAccrual {

    private Long id;
    private final Long patronId;
    private final Long companionId;
    private final LocalDate statDate;
    private BigDecimal accrued;

    private IntimacyDailyAccrual(Long id, Long patronId, Long companionId, LocalDate statDate, BigDecimal accrued) {
        this.id = id;
        this.patronId = patronId;
        this.companionId = companionId;
        this.statDate = statDate;
        this.accrued = accrued;
    }

    public static IntimacyDailyAccrual initiate(Long patronId, Long companionId, LocalDate statDate) {
        return new IntimacyDailyAccrual(null, patronId, companionId, statDate, BigDecimal.ZERO);
    }

    public static IntimacyDailyAccrual restore(Long id, Long patronId, Long companionId,
                                                LocalDate statDate, BigDecimal accrued) {
        return new IntimacyDailyAccrual(id, patronId, companionId, statDate, accrued);
    }

    /**
     * 在不超过 dailyCap 的前提下累加，返回实际被允许计入的数值（可能小于 requested）。
     * 调用方应该只把返回值累加进 IntimacyRecord.progressValue，不是原始 requested。
     */
    public BigDecimal accrueUpTo(BigDecimal requested, BigDecimal dailyCap) {
        BigDecimal allowed = dailyCap.subtract(accrued).max(BigDecimal.ZERO);
        BigDecimal applied = requested.min(allowed);
        this.accrued = this.accrued.add(applied);
        return applied;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getPatronId() {
        return patronId;
    }

    public Long getCompanionId() {
        return companionId;
    }

    public LocalDate getStatDate() {
        return statDate;
    }

    public BigDecimal getAccrued() {
        return accrued;
    }
}
