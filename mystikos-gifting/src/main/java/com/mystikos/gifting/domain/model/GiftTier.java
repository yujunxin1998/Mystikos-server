package com.mystikos.gifting.domain.model;

import java.math.BigDecimal;

/**
 * 礼物档位——运营配置数据。倍率决定同样星辉石金额的礼物能换到多少亲密度
 * （见 GiftApplicationService#sendGift 里 intimacyValue 的计算），不是一条阈值阶梯，
 * 所以不实现 {@code LevelTier}——档位是平的分类，不是"累计值→挡位"的梯子。
 */
public class GiftTier {

    private final Long id;
    private final String code;
    private final String displayName;
    private final String displayNameEn;
    private final BigDecimal multiplier;
    private final int sortOrder;
    private final boolean active;

    public GiftTier(Long id, String code, String displayName, String displayNameEn,
                     BigDecimal multiplier, int sortOrder, boolean active) {
        this.id = id;
        this.code = code;
        this.displayName = displayName;
        this.displayNameEn = displayNameEn;
        this.multiplier = multiplier;
        this.sortOrder = sortOrder;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisplayNameEn() {
        return displayNameEn;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public boolean isActive() {
        return active;
    }
}
