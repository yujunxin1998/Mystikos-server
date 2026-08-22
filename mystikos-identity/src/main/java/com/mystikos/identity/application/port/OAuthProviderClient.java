package com.mystikos.identity.application.port;

/**
 * 出站端口：拿第三方授权码换用户信息。每个 Provider（Discord/微信……）一个实现，
 * {@link #providerCode()} 对应 {@code /api/v1/auth/oauth/{provider}/login} 路径里的 provider 段
 * （大小写不敏感），AuthApplicationService 按这个把请求路由到对应实现，
 * 没有匹配的实现就是"该第三方登录方式暂未开放"。
 */
public interface OAuthProviderClient {

    String providerCode();

    OAuthUserInfo exchangeCodeForUser(String authorizationCode);
}
