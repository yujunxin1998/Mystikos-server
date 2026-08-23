package com.mystikos.membership.domain.model;

import com.mystikos.common.membership.MembershipTier;

import java.math.BigDecimal;

/**
 * {@link MembershipTier} 接口的默认实现——门槛是占位值（业务确认真实梯度前的临时数字，
 * 见需求讨论：先占位，后续随时可改，改这个枚举即可，不用动调用方）。
 * 枚举常量名本身就是 {@link #getCode()} 的值，方便按事件里的字符串 code 反查
 * （见 mystikos-identity 的 MembershipTierUpgradedEventListener）。
 */
public enum DefaultMembershipTier implements MembershipTier {

    LV1(1, "入门搭子", BigDecimal.valueOf(0)),
    LV2(2, "常客", BigDecimal.valueOf(500)),
    LV3(3, "挚友", BigDecimal.valueOf(2000)),
    LV4(4, "核心玩家", BigDecimal.valueOf(8000)),
    LV5(5, "传奇搭档", BigDecimal.valueOf(30000));

    private final int level;
    private final String displayName;
    private final BigDecimal minimumCumulativeSpend;

    DefaultMembershipTier(int level, String displayName, BigDecimal minimumCumulativeSpend) {
        this.level = level;
        this.displayName = displayName;
        this.minimumCumulativeSpend = minimumCumulativeSpend;
    }

    /** 按累计消费金额解析出应处的等级——取满足门槛里最高的一档。 */
    public static DefaultMembershipTier resolveByCumulativeSpend(BigDecimal cumulativeSpend) {
        DefaultMembershipTier resolved = LV1;
        for (DefaultMembershipTier tier : values()) {
            if (cumulativeSpend.compareTo(tier.minimumCumulativeSpend) >= 0) {
                resolved = tier;
            }
        }
        return resolved;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getMinimumCumulativeSpend() {
        return minimumCumulativeSpend;
    }
}
