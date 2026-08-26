package com.mystikos.booking.domain.model;

import com.mystikos.common.dict.DictEnum;

/**
 * DRAFT → PENDING_PAYMENT → PAID → MATCHING → ACCEPTED → IN_SERVICE → COMPLETED，
 * 旁路 CANCELLED / EXPIRED / DISPUTED / REFUNDED（见 docs/architecture/domain-model.md）。
 */
public enum BookingStatus implements DictEnum {
    DRAFT("DRAFT", "草稿"),
    PENDING_PAYMENT("PENDING_PAYMENT", "待支付"),
    PAID("PAID", "已支付"),
    MATCHING("MATCHING", "匹配中"),
    ACCEPTED("ACCEPTED", "已接单"),
    IN_SERVICE("IN_SERVICE", "服务中"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消"),
    EXPIRED("EXPIRED", "已失效"),
    DISPUTED("DISPUTED", "争议中"),
    REFUNDED("REFUNDED", "已退款");

    private final String code;
    private final String displayName;

    BookingStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}
