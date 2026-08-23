package com.mystikos.identity.domain;

import com.mystikos.common.result.IResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Identity 上下文错误码，号段 2000-2999（见 docs/architecture/exception-handling.md）。
 */
@Getter
@AllArgsConstructor
public enum IdentityResponseCode implements IResponseCode {

    IDENTIFIER_ALREADY_EXISTS(2001, "手机号或邮箱已被注册"),
    USER_NOT_FOUND(2002, "用户不存在"),
    ROLE_NOT_ASSIGNED(2003, "用户未拥有该角色，无法移除"),
    LAST_ROLE_CANNOT_BE_REMOVED(2004, "用户至少需要保留一个角色"),
    MEMBER_ROLE_REQUIRED(2005, "用户不是会员角色，无法设置会员等级"),
    UNKNOWN_ROLE_CODE(2006, "未知的角色编码"),
    IDENTIFIER_REQUIRED(2007, "手机号、邮箱和第三方绑定至少需要一项"),
    VERIFICATION_CODE_INVALID(2008, "验证码错误或已使用"),
    VERIFICATION_CODE_EXPIRED(2009, "验证码已过期，请重新获取"),
    CREDENTIAL_INVALID(2010, "账号或密码/验证码错误"),
    ACCOUNT_NOT_ACTIVE(2011, "账号当前状态不允许登录"),
    REFRESH_TOKEN_INVALID(2012, "登录状态已失效，请重新登录"),
    OAUTH_PROVIDER_NOT_CONFIGURED(2013, "该第三方登录方式暂未开放"),
    VERIFICATION_CODE_RATE_LIMITED(2014, "验证码请求过于频繁，请稍后再试"),
    LOGIN_RATE_LIMITED(2015, "登录尝试过于频繁，请稍后再试"),
    OAUTH_EXCHANGE_FAILED(2016, "第三方登录授权失败，请重新尝试"),
    TAG_NOT_FOUND(2017, "标签不存在"),
    TAG_DISABLED(2018, "标签已停用，不能选择"),
    REGION_NOT_FOUND(2019, "行政区划编码不存在"),
    COMPANION_PROFILE_NOT_FOUND(2020, "打手资料不存在");

    private final int code;
    private final String message;
}
