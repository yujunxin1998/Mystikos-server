package com.mystikos.identity.domain.model;

import com.mystikos.common.dict.DictEnum;

/** 打手接单状态，跟账号启用状态（{@link UserStatus}）是两回事——账号被封禁不等于接单状态变化。 */
public enum CompanionStatus implements DictEnum {
    AVAILABLE("AVAILABLE", "可接单"),
    BUSY("BUSY", "服务中"),
    OFFLINE("OFFLINE", "已下线");

    private final String code;
    private final String displayName;

    CompanionStatus(String code, String displayName) {
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
