package com.mystikos.identity.domain.model;

/** 打手接单状态，跟账号启用状态（{@link UserStatus}）是两回事——账号被封禁不等于接单状态变化。 */
public enum CompanionStatus {
    AVAILABLE,
    BUSY,
    OFFLINE
}
