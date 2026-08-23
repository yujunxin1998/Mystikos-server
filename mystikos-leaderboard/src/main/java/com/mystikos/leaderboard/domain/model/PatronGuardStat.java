package com.mystikos.leaderboard.domain.model;

import java.math.BigDecimal;

/** 老板守护值累计统计，见 {@link CompanionCharmStat} 的说明，结构对称。 */
public class PatronGuardStat {

    private final Long patronId;
    private BigDecimal guardValue;

    private PatronGuardStat(Long patronId, BigDecimal guardValue) {
        this.patronId = patronId;
        this.guardValue = guardValue;
    }

    public static PatronGuardStat initiate(Long patronId) {
        return new PatronGuardStat(patronId, BigDecimal.ZERO);
    }

    public static PatronGuardStat restore(Long patronId, BigDecimal guardValue) {
        return new PatronGuardStat(patronId, guardValue);
    }

    public void accrue(BigDecimal amount) {
        this.guardValue = this.guardValue.add(amount);
    }

    public Long getPatronId() {
        return patronId;
    }

    public BigDecimal getGuardValue() {
        return guardValue;
    }
}
