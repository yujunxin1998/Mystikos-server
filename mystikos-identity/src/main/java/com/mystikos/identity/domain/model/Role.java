package com.mystikos.identity.domain.model;

import com.mystikos.identity.domain.IdentityException;

import java.util.Arrays;

/**
 * 固定的角色集合，由业务给定，不做成运行时可增删的动态角色表。
 * 每个角色具体能做什么由 identity_role_permission 表驱动，落地时不预置任何
 * 权限编码——业务没定义就不要在框架里编。
 *
 * 会员等级不在这里：见 {@link com.mystikos.common.membership.MembershipTier}。
 */
public enum Role {

    /** 未登录/匿名访问的默认权限集合，一般不落库为某个用户的角色分配。 */
    GUEST("GUEST", "游客"),
    MEMBER("MEMBER", "会员用户"),
    COMPANION("COMPANION", "陪玩"),
    CUSTOMER_SERVICE("CUSTOMER_SERVICE", "客服"),
    ASSESSOR("ASSESSOR", "考核官"),
    ADMIN("ADMIN", "管理员");

    private final String code;
    private final String displayName;

    Role(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromCode(String code) {
        return Arrays.stream(values())
                .filter(role -> role.code.equals(code))
                .findFirst()
                .orElseThrow(() -> IdentityException.unknownRoleCode(code));
    }
}
