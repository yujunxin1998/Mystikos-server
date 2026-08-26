package com.mystikos.identity.application.service;

/** 第三方账号绑定/换绑成功后的结果，供一次性票据兑换接口返回给前端展示。 */
public record OAuthBindOutcome(String provider, String providerUserId, String displayName) {
}
