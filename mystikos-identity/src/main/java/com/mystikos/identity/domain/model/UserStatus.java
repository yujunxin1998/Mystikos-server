package com.mystikos.identity.domain.model;

public enum UserStatus {
    ACTIVE,
    DISABLED,
    BANNED,
    /** 管理员删除用户，逻辑删除，不物理删行。 */
    DELETED
}
