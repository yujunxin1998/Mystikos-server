package com.mystikos.gifting.domain.model;

import java.math.BigDecimal;

/**
 * 解锁规则值对象。{@code threshold} 在 {@link UnlockRuleType#NONE} 时可以为空，
 * 其余类型必须给出阈值——具体单位随类型变化（次数/金额/天数/名次/阶段）。
 */
public record UnlockRule(UnlockRuleType type, BigDecimal threshold) {

    public UnlockRule {
        if (type != UnlockRuleType.NONE && threshold == null) {
            throw new IllegalArgumentException("非 NONE 类型的解锁规则必须给出阈值");
        }
    }

    public static UnlockRule none() {
        return new UnlockRule(UnlockRuleType.NONE, null);
    }

    /** 该规则是否属于当前已实现评估逻辑的类型集合，见 {@link UnlockRuleType} 的说明。 */
    public boolean isEvaluable() {
        return type == UnlockRuleType.NONE
                || type == UnlockRuleType.CUMULATIVE_COUNT
                || type == UnlockRuleType.CUMULATIVE_SPEND;
    }

    /**
     * 判定是否满足解锁条件。只处理 {@link #isEvaluable()} 为 true 的类型，
     * 调用方要先自行拦截不可评估的类型（见 GiftApplicationService）。
     */
    public boolean isSatisfiedBy(long cumulativeCount, BigDecimal cumulativeSpend) {
        return switch (type) {
            case NONE -> true;
            case CUMULATIVE_COUNT -> BigDecimal.valueOf(cumulativeCount).compareTo(threshold) >= 0;
            case CUMULATIVE_SPEND -> cumulativeSpend.compareTo(threshold) >= 0;
            default -> throw new IllegalStateException("规则类型 " + type + " 暂不支持评估，调用方应提前拦截");
        };
    }
}
