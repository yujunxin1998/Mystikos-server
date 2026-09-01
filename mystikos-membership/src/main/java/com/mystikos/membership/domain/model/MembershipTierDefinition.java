package com.mystikos.membership.domain.model;

import com.mystikos.common.level.LevelTier;

import java.math.BigDecimal;

/**
 * VIP 等级定义——运营配置数据（数据库表），取代原来的 {@code DefaultMembershipTier} 枚举。
 * 新增/调整等级门槛或权益文案只是这张表的增删改，不需要发版；
 * {@link com.mystikos.common.level.LevelResolver} 用它做"累计消费 → 当前等级"的解析。
 *
 * <p>{@code level} 字段保留是因为 {@link com.mystikos.common.membership.MembershipTier}
 * 接口需要一个可比较的数值（{@code mystikos-identity} 的 {@code User.membershipTierLevel}
 * 就存这个数），实际排序/解析仍然用 {@link #getSortOrder()}——两者当前取值相同，
 * 但含义不同：level 是对外展示的等级数值，sortOrder 是内部解析用的位次。
 */
public class MembershipTierDefinition implements LevelTier, com.mystikos.common.membership.MembershipTier {

    private final Long id;
    private final String code;
    private final String displayName;
    private final String displayNameEn;
    private final int level;
    private final BigDecimal cumulativeSpendThreshold;
    private final String perkDescription;
    private final int sortOrder;

    public MembershipTierDefinition(Long id, String code, String displayName, String displayNameEn,
                                     int level, BigDecimal cumulativeSpendThreshold,
                                     String perkDescription, int sortOrder) {
        this.id = id;
        this.code = code;
        this.displayName = displayName;
        this.displayNameEn = displayNameEn;
        this.level = level;
        this.cumulativeSpendThreshold = cumulativeSpendThreshold;
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
        return displayName;
    }

    public String getDisplayNameEn() {
        return displayNameEn;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public BigDecimal getThreshold() {
        return cumulativeSpendThreshold;
    }

    public String getPerkDescription() {
        return perkDescription;
    }

    @Override
    public int getSortOrder() {
        return sortOrder;
    }
}
