package com.mystikos.identity.infrastructure.acl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mystikos.identity.application.port.OAuthProviderClient;
import com.mystikos.identity.application.port.OAuthUserInfo;
import com.mystikos.identity.domain.IdentityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;

/**
 * Discord OAuth2 授权码换用户信息。标准的 authorization_code 流程：
 * 前端把用户导到 Discord 授权页拿到 code，POST 到我们的
 * {@code /api/v1/auth/oauth/discord/login}，这里拿 code 换 access_token 再换用户信息。
 *
 * <p>redirect-uri 必须和前端发起授权时用的那个 URI 完全一致（也要跟 Discord Developer Portal
 * 里应用配置的 redirect 一致），Discord 校验这个是防授权码被挪用的关键一步，不是随便填的占位符。
 *
 * <p>只在配置了 client-id 时注册这个 Bean——没配置就让 AuthApplicationService 的
 * provider 路由表里找不到 "discord"，走"该第三方登录方式暂未开放"这条已有路径，
 * 而不是拿一个空 client-id/secret 去调 Discord API 换回一个更难懂的 400。
 */
@Component
@ConditionalOnExpression("!'${mystikos.oauth.discord.client-id:}'.isEmpty()")
public class DiscordOAuthClient implements OAuthProviderClient {

    private static final String TOKEN_URL = "https://discord.com/api/oauth2/token";
    private static final String USER_INFO_URL = "https://discord.com/api/users/@me";
    private static final String AUTHORIZE_URL = "https://discord.com/oauth2/authorize";

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public DiscordOAuthClient(@Value("${mystikos.oauth.discord.client-id}") String clientId,
                               @Value("${mystikos.oauth.discord.client-secret}") String clientSecret,
                               @Value("${mystikos.oauth.discord.redirect-uri}") String redirectUri,
                               @Value("${mystikos.oauth.proxy-host:}") String proxyHost,
                               @Value("${mystikos.oauth.proxy-port:0}") int proxyPort) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.restClient = createRestClient(proxyHost, proxyPort);
    }

    @Override
    public String providerCode() {
        return "discord";
    }

    @Override
    public OAuthUserInfo exchangeCodeForUser(String authorizationCode) {
        return exchangeCodeForUser(authorizationCode, null);
    }

    @Override
    public URI buildAuthorizationUri(String state, String codeChallenge) {
        return UriComponentsBuilder.fromUriString(AUTHORIZE_URL)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "identify email")
                .queryParam("state", state)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .build()
                .encode()
                .toUri();
    }

    @Override
    public OAuthUserInfo exchangeCodeForUser(String authorizationCode, String codeVerifier) {
        String accessToken = exchangeCodeForAccessToken(authorizationCode, codeVerifier);
        DiscordUser discordUser = fetchDiscordUser(accessToken);
        return new OAuthUserInfo(discordUser.id(), discordUser.username(), discordUser.email());
    }

    private String exchangeCodeForAccessToken(String authorizationCode, String codeVerifier) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("grant_type", "authorization_code");
        form.add("code", authorizationCode);
        form.add("redirect_uri", redirectUri);
        if (codeVerifier != null && !codeVerifier.isBlank()) {
            form.add("code_verifier", codeVerifier);
        }

        TokenResponse token;
        try {
            token = restClient.post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TokenResponse.class);
        } catch (RestClientException e) {
            throw IdentityException.oauthExchangeFailed("discord", e.getMessage());
        }
        if (token == null || token.accessToken() == null) {
            throw IdentityException.oauthExchangeFailed("discord", "empty token response");
        }
        return token.accessToken();
    }

    private DiscordUser fetchDiscordUser(String accessToken) {
        DiscordUser discordUser;
        try {
            discordUser = restClient.get()
                    .uri(USER_INFO_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(DiscordUser.class);
        } catch (RestClientException e) {
            throw IdentityException.oauthExchangeFailed("discord", e.getMessage());
        }
        if (discordUser == null || discordUser.id() == null) {
            throw IdentityException.oauthExchangeFailed("discord", "empty user response");
        }
        return discordUser;
    }

    private RestClient createRestClient(String proxyHost, int proxyPort) {
        if (proxyHost == null || proxyHost.isBlank() || proxyPort <= 0) {
            return RestClient.create();
        }
        HttpClient httpClient = HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort)))
                .build();
        return RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }

    private record TokenResponse(@JsonProperty("access_token") String accessToken) {
    }

    private record DiscordUser(String id, String username, String email) {
    }
}
