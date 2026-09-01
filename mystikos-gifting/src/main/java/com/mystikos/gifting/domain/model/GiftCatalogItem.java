package com.mystikos.gifting.domain.model;

import java.math.BigDecimal;

/**
 * 礼物目录条目聚合根。目录是运营配置数据，本身不建模状态机——上下架用
 * {@code active} 布尔位，不需要更复杂的状态流转。{@code tierId} 引用 {@link GiftTier}，
 * 同一档位内新增/调整礼物只是增删改这张表的行，不影响倍率本身。
 */
public class GiftCatalogItem {

    private final Long id;
    private final String code;
    private final String name;
    private final String icon;
    private final BigDecimal price;
    private final Long tierId;
    private final UnlockRule unlockRule;
    private final boolean active;

    public GiftCatalogItem(Long id, String code, String name, String icon, BigDecimal price,
                            Long tierId, UnlockRule unlockRule, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.icon = icon;
        this.price = price;
        this.tierId = tierId;
        this.unlockRule = unlockRule;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Long getTierId() {
        return tierId;
    }

    public UnlockRule getUnlockRule() {
        return unlockRule;
    }

    public boolean isActive() {
        return active;
    }
}
