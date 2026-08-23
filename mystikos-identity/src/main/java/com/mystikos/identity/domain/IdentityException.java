package com.mystikos.identity.domain;

import com.mystikos.common.web.exception.BusinessException;
import com.mystikos.identity.domain.model.CompanionIdentityApplicationStatus;
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

    public static IdentityException oauthTransactionInvalid() {
        return new IdentityException(IdentityResponseCode.OAUTH_TRANSACTION_INVALID);
    }

    public static IdentityException tagNotFound(Long tagId) {
        return new IdentityException(IdentityResponseCode.TAG_NOT_FOUND, "标签不存在：" + tagId);
    }

    public static IdentityException tagDisabled(Long tagId) {
        return new IdentityException(IdentityResponseCode.TAG_DISABLED, "标签已停用：" + tagId);
    }

    public static IdentityException regionNotFound(String regionCode) {
        return new IdentityException(IdentityResponseCode.REGION_NOT_FOUND, "行政区划编码不存在：" + regionCode);
    }

    public static IdentityException companionProfileNotFound(Long userId) {
        return new IdentityException(IdentityResponseCode.COMPANION_PROFILE_NOT_FOUND, "打手资料不存在：" + userId);
    }

    public static IdentityException companionApplicationNotFound(Long id) {
        return new IdentityException(IdentityResponseCode.COMPANION_APPLICATION_NOT_FOUND, "陪玩申请不存在：" + id);
    }

    public static IdentityException companionApplicationContactRequired() {
        return new IdentityException(IdentityResponseCode.COMPANION_APPLICATION_CONTACT_REQUIRED);
    }

    public static IdentityException companionApplicationProfileIncomplete() {
        return new IdentityException(IdentityResponseCode.COMPANION_APPLICATION_PROFILE_INCOMPLETE);
    }

    public static IdentityException companionApplicationAlreadyPending() {
        return new IdentityException(IdentityResponseCode.COMPANION_APPLICATION_ALREADY_PENDING);
    }

    public static IdentityException companionApplicationAlreadyCompanion() {
        return new IdentityException(IdentityResponseCode.COMPANION_APPLICATION_ALREADY_COMPANION);
    }

    public static IdentityException companionApplicationInvalidStatusTransition(
            CompanionIdentityApplicationStatus status) {
        return new IdentityException(IdentityResponseCode.COMPANION_APPLICATION_INVALID_STATUS_TRANSITION,
                "当前状态不允许该操作：" + status);
    }

    public static IdentityException companionApplicationReviewerRequired() {
        return new IdentityException(IdentityResponseCode.COMPANION_APPLICATION_REVIEWER_REQUIRED);
    }

    public static IdentityException companionApplicationResultRequired() {
        return new IdentityException(IdentityResponseCode.COMPANION_APPLICATION_RESULT_REQUIRED);
    }

    public static IdentityException companionApplicationReviewerNotAssessor(Long reviewerId) {
        return new IdentityException(IdentityResponseCode.COMPANION_APPLICATION_REVIEWER_NOT_ASSESSOR,
                "考核人必须是考核官或管理员角色：" + reviewerId);
    }
}
