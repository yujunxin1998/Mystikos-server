package com.mystikos.membership.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** 会员成长账户聚合根，与老板用户 1:1。 */
public class MembershipAccount {

    private Long id;
    private final Long patronId;
    private DefaultMembershipTier currentTier;
    private BigDecimal cumulativeSpend;
    private OffsetDateTime tierUpgradedAt;

    private MembershipAccount(Long id, Long patronId, DefaultMembershipTier currentTier,
                               BigDecimal cumulativeSpend, OffsetDateTime tierUpgradedAt) {
        this.id = id;
        this.patronId = patronId;
        this.currentTier = currentTier;
        this.cumulativeSpend = cumulativeSpend;
        this.tierUpgradedAt = tierUpgradedAt;
    }

    public static MembershipAccount initiate(Long patronId) {
        return new MembershipAccount(null, patronId, DefaultMembershipTier.LV1, BigDecimal.ZERO, OffsetDateTime.now());
    }

    /** 从持久化数据重建聚合，仅供仓储实现调用。 */
    public static MembershipAccount restore(Long id, Long patronId, DefaultMembershipTier currentTier,
                                             BigDecimal cumulativeSpend, OffsetDateTime tierUpgradedAt) {
        return new MembershipAccount(id, patronId, currentTier, cumulativeSpend, tierUpgradedAt);
    }

    /**
     * 累加消费并按新的累计值重算等级。
     * @return 是否发生升级（降级不存在——累计消费只会增长，见 accrueSpend 的调用方）
     */
    public boolean accrueSpend(BigDecimal amount) {
        this.cumulativeSpend = this.cumulativeSpend.add(amount);
        DefaultMembershipTier resolved = DefaultMembershipTier.resolveByCumulativeSpend(this.cumulativeSpend);
        if (resolved != this.currentTier) {
            this.currentTier = resolved;
            this.tierUpgradedAt = OffsetDateTime.now();
            return true;
        }
        return false;
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

    public DefaultMembershipTier getCurrentTier() {
        return currentTier;
    }

    public BigDecimal getCumulativeSpend() {
        return cumulativeSpend;
    }

    public OffsetDateTime getTierUpgradedAt() {
        return tierUpgradedAt;
    }
}
