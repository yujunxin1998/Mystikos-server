package com.mystikos.relationship.domain.model;

import com.mystikos.common.level.LevelTier;

import java.math.BigDecimal;

/**
 * 亲密度等级定义——运营配置数据（数据库表，不是枚举）。新增/调整等级门槛或权益文案
 * 只是这张表的增删改，不需要发版；{@link com.mystikos.common.level.LevelResolver}
 * 用它做"累计进度 → 当前等级"的解析。
 */
public class IntimacyLevelDefinition implements LevelTier {

    private final Long id;
    private final String code;
    private final String displayNameZh;
    private final String displayNameEn;
    private final BigDecimal threshold;
    private final String perkDescription;
    private final int sortOrder;

    public IntimacyLevelDefinition(Long id, String code, String displayNameZh, String displayNameEn,
                                    BigDecimal threshold, String perkDescription, int sortOrder) {
        this.id = id;
        this.code = code;
        this.displayNameZh = displayNameZh;
        this.displayNameEn = displayNameEn;
        this.threshold = threshold;
        this.perkDescription = perkDescription;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDisplayName() {
        return displayNameZh;
    }

    public String getDisplayNameEn() {
        return displayNameEn;
    }

    @Override
    public BigDecimal getThreshold() {
        return threshold;
    }

    public String getPerkDescription() {
        return perkDescription;
    }

    @Override
    public int getSortOrder() {
        return sortOrder;
    }
}
