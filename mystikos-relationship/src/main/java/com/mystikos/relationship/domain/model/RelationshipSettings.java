package com.mystikos.relationship.domain.model;

import java.math.BigDecimal;

/**
 * 单行配置——只有一条记录（id 固定为 1）。每日亲密度上限是防刷参数，运营随时可能调整，
 * 放这里而不是 Java 常量，改一行 UPDATE 就生效，不用发版。
 */
public class RelationshipSettings {

    private final BigDecimal dailyIntimacyCap;

    public RelationshipSettings(BigDecimal dailyIntimacyCap) {
        this.dailyIntimacyCap = dailyIntimacyCap;
    }

    public static RelationshipSettings defaults() {
        return new RelationshipSettings(BigDecimal.valueOf(20000));
    }

    public BigDecimal getDailyIntimacyCap() {
        return dailyIntimacyCap;
    }
}
