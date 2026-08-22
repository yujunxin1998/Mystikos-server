package com.mystikos.identity.domain.model;

import java.time.OffsetDateTime;

/**
 * 第三方登录绑定。provider 用字符串不用枚举——具体接哪些第三方（Discord/微信……）
 * 还没定，新增供应商不应该要求改这个值对象。
 */
public record OAuthBinding(String provider, String providerUserId, OffsetDateTime boundAt) {
}
