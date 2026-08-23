package com.mystikos.identity.domain.model;

/**
 * 陪玩身份台账（{@link CompanionProfile}）的身份状态：ACTIVE 表示当前拥有陪玩身份，
 * REVOKED 表示身份已被收回（软删除，业务资料保留，方便重新开通时复用）。
 * 跟 {@link CompanionStatus} 是两回事——那个是接单中/忙碌/离线的业务状态。
 */
public enum CompanionIdentityStatus {
    ACTIVE,
    REVOKED
}
