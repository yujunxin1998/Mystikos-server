package com.mystikos.identity.application.port;

/**
 * 第三方授权码换回来的用户信息。email 可能是 null——不是所有 Provider/授权范围都会给邮箱，
 * 也不会自动写进 {@code identity_user.email}（那个字段走我们自己的验证码校验流程，
 * 不能被第三方数据未经验证就顶替）。
 */
public record OAuthUserInfo(String providerUserId, String displayName, String email) {
}
