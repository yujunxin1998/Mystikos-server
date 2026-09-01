package com.mystikos.common.level;

import java.math.BigDecimal;

/**
 * 阶梯型等级契约：给定一组按 threshold 排列的档位，找出不超过某个累计值的最高档。
 * Membership 的 VIP 等级、Relationship 的亲密度等级结构完全相同（都是"累计值 → 挡位"），
 * 只是含义不同，共用 {@link LevelResolver} 这一套解析算法，避免各自维护一份
 * "找最高满足阈值的档位"的实现（这两个上下文以前各自写了一份，逻辑逐行雷同）。
 */
public interface LevelTier {

    String getCode();

    String getDisplayName();

    /** 排序位次，决定同一批档位里谁"更高"——不直接用 threshold 排序，允许阈值以外的排序需求。 */
    int getSortOrder();

    BigDecimal getThreshold();
}
