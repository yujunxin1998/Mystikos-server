package com.mystikos.identity.domain;

import com.mystikos.common.web.exception.BusinessException;
import com.mystikos.identity.domain.model.Role;

public class IdentityException extends BusinessException {

    public IdentityException(IdentityResponseCode code) {
        super(code);
    }

    public IdentityException(IdentityResponseCode code, String message) {
        super(code, message);
    }

    public static IdentityException identifierAlreadyExists(String identifier) {
        return new IdentityException(IdentityResponseCode.IDENTIFIER_ALREADY_EXISTS,
                "手机号或邮箱已被注册：" + identifier);
    }

    public static IdentityException identifierRequired() {
        return new IdentityException(IdentityResponseCode.IDENTIFIER_REQUIRED);
    }

    public static IdentityException notFound(Long userId) {
        return new IdentityException(IdentityResponseCode.USER_NOT_FOUND,
                "用户不存在：" + userId);
    }

    public static IdentityException roleNotAssigned(Role role) {
        return new IdentityException(IdentityResponseCode.ROLE_NOT_ASSIGNED,
                "用户未拥有角色：" + role.getCode());
    }

    public static IdentityException lastRoleCannotBeRemoved(Role role) {
        return new IdentityException(IdentityResponseCode.LAST_ROLE_CANNOT_BE_REMOVED,
                "不能移除角色 " + role.getCode() + "：用户至少需要保留一个角色");
    }

    public static IdentityException notMember(Long userId) {
        return new IdentityException(IdentityResponseCode.MEMBER_ROLE_REQUIRED);
    }

    public static IdentityException unknownRoleCode(String code) {
        return new IdentityException(IdentityResponseCode.UNKNOWN_ROLE_CODE,
                "未知的角色编码：" + code);
    }

    public static IdentityException verificationCodeInvalid() {
        return new IdentityException(IdentityResponseCode.VERIFICATION_CODE_INVALID);
    }

    public static IdentityException verificationCodeExpired() {
        return new IdentityException(IdentityResponseCode.VERIFICATION_CODE_EXPIRED);
    }

    public static IdentityException credentialInvalid() {
        return new IdentityException(IdentityResponseCode.CREDENTIAL_INVALID);
    }

    public static IdentityException accountNotActive() {
        return new IdentityException(IdentityResponseCode.ACCOUNT_NOT_ACTIVE);
    }

    public static IdentityException refreshTokenInvalid() {
        return new IdentityException(IdentityResponseCode.REFRESH_TOKEN_INVALID);
    }

    public static IdentityException oauthProviderNotConfigured(String provider) {
        return new IdentityException(IdentityResponseCode.OAUTH_PROVIDER_NOT_CONFIGURED,
                "第三方登录方式暂未开放：" + provider);
    }

    public static IdentityException verificationCodeRateLimited() {
        return new IdentityException(IdentityResponseCode.VERIFICATION_CODE_RATE_LIMITED);
    }

    public static IdentityException loginRateLimited() {
        return new IdentityException(IdentityResponseCode.LOGIN_RATE_LIMITED);
    }

    public static IdentityException oauthExchangeFailed(String provider, String reason) {
        return new IdentityException(IdentityResponseCode.OAUTH_EXCHANGE_FAILED,
                "第三方登录授权失败（" + provider + "）：" + reason);
    }
}
