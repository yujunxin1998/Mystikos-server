package com.mystikos.identity.domain.model;

import com.mystikos.common.dict.DictEnum;

public enum UserStatus implements DictEnum {
    ACTIVE("ACTIVE", "正常"),
    DISABLED("DISABLED", "已禁用"),
    BANNED("BANNED", "已封禁"),
    /** 管理员删除用户，逻辑删除，不物理删行。 */
    DELETED("DELETED", "已删除");

    private final String code;
    private final String displayName;

    UserStatus(String code, String displayName) {
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
