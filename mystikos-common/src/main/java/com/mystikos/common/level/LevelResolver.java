package com.mystikos.common.level;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * 纯函数：在一组 {@link LevelTier} 里找到 threshold &lt;= cumulativeValue 里
 * sortOrder 最大的一档。调用方（Membership/Relationship 的应用服务）负责加载
 * 档位列表，这里不做任何 IO。
 */
public final class LevelResolver {

    private LevelResolver() {
    }

    /**
     * @param tiers          候选档位，顺序不作要求，内部按 sortOrder 升序处理
     * @param cumulativeValue 当前累计值
     * @return 满足条件里 sortOrder 最大的一档
     * @throws IllegalArgumentException tiers 为空
     * @throws IllegalStateException    没有任何一档的 threshold &lt;= cumulativeValue
     *                                   （调用方必须保证存在一档 threshold&lt;=0 的兜底档）
     */
    public static <T extends LevelTier> T resolve(List<T> tiers, BigDecimal cumulativeValue) {
        if (tiers == null || tiers.isEmpty()) {
            throw new IllegalArgumentException("等级梯度不能为空");
        }
        T resolved = null;
        for (T tier : tiers.stream().sorted(Comparator.comparingInt(LevelTier::getSortOrder)).toList()) {
            if (cumulativeValue.compareTo(tier.getThreshold()) >= 0) {
                resolved = tier;
            }
        }
        if (resolved == null) {
            throw new IllegalStateException(
                    "累计值 " + cumulativeValue + " 未匹配到任何等级，检查是否存在 threshold<=0 的兜底档");
        }
        return resolved;
    }
}
