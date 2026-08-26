package com.mystikos.identity.domain.model;

public enum VerificationPurpose {
    REGISTER,
    LOGIN,
    RESET_PASSWORD,
    BIND_CONTACT,
    /** 第三方账号绑定/换绑/解绑前的二次确认，验证码发到用户已有的邮箱或手机号。 */
    OAUTH_BINDING_CHANGE
}
