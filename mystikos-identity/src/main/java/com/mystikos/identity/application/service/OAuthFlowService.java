package com.mystikos.identity.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mystikos.identity.application.port.OAuthProviderClient;
import com.mystikos.identity.domain.IdentityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manages server-side OAuth state, PKCE and short-lived one-time tickets.
 *
 * <p>State 现在还要携带 {@code purpose}（登录 or 绑定）和发起绑定的 {@code userId}，
 * 这样 Discord 回调（无认证的浏览器重定向，带不了 Bearer token）也能知道要把绑定结果
 * 落到哪个用户身上——userId 是在"发起绑定"这一步（认证态的 POST 请求）里从
 * {@code CurrentUserContext} 取出来存进 Redis state 的，不依赖回调请求本身的认证信息。
 */
@Service
public class OAuthFlowService {

    private static final Duration STATE_TTL = Duration.ofMinutes(5);
    private static final Duration TICKET_TTL = Duration.ofMinutes(1);
    private static final String STATE_PREFIX = "identity:oauth:state:";
    private static final String TICKET_PREFIX = "identity:oauth:ticket:";
    private static final String BIND_TICKET_PREFIX = "identity:oauth:bindticket:";

    private enum Purpose { LOGIN, BIND }

    private record AuthorizationState(String codeVerifier, Purpose purpose, Long userId) {
    }

    private final AuthApplicationService authApplicationService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, OAuthProviderClient> providerClients;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String frontendReturnUri;

    public OAuthFlowService(AuthApplicationService authApplicationService,
                            StringRedisTemplate redisTemplate,
                            ObjectMapper objectMapper,
                            List<OAuthProviderClient> providerClients,
                            @Value("${mystikos.oauth.frontend-return-uri:}")
                            String frontendReturnUri) {
        this.authApplicationService = authApplicationService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.providerClients = providerClients.stream().collect(Collectors.toMap(
                client -> client.providerCode().toLowerCase(Locale.ROOT), Function.identity()));
        this.frontendReturnUri = frontendReturnUri;
    }

    public URI beginAuthorization(String provider) {
        OAuthProviderClient client = provider(provider);
        String state = randomToken(32);
        String codeVerifier = randomToken(64);
        String codeChallenge = base64Url(sha256(codeVerifier.getBytes(StandardCharsets.US_ASCII)));
        storeState(provider, state, new AuthorizationState(codeVerifier, Purpose.LOGIN, null));
        return client.buildAuthorizationUri(state, codeChallenge);
    }

    /**
     * 发起"绑定/换绑第三方账号"授权。调用方必须是已登录用户的认证请求（校验二次确认验证码
     * 也在这一步完成），返回的只是授权 URL，由前端自己跳转——这样就不需要 Discord 回调
     * 那个匿名 GET 请求携带 Bearer token。
     */
    public URI beginBindAuthorization(String provider, Long userId, String verificationCode) {
        OAuthProviderClient client = provider(provider);
        authApplicationService.consumeOAuthBindingVerificationCode(userId, verificationCode);
        String state = randomToken(32);
        String codeVerifier = randomToken(64);
        String codeChallenge = base64Url(sha256(codeVerifier.getBytes(StandardCharsets.US_ASCII)));
        storeState(provider, state, new AuthorizationState(codeVerifier, Purpose.BIND, userId));
        return client.buildAuthorizationUri(state, codeChallenge);
    }

    public URI completeAuthorization(String provider, String code, String state) {
        if (frontendReturnUri == null || frontendReturnUri.isBlank()) {
            throw new IllegalStateException("OAUTH_FRONTEND_RETURN_URI is required for OAuth login");
        }
        String raw = redisTemplate.opsForValue().getAndDelete(stateKey(provider, state));
        if (raw == null) {
            throw IdentityException.oauthTransactionInvalid();
        }
        AuthorizationState authorizationState = readState(raw);

        if (authorizationState.purpose() == Purpose.BIND) {
            OAuthBindOutcome outcome = authApplicationService.bindOAuthProvider(
                    authorizationState.userId(), provider, code, authorizationState.codeVerifier());
            String ticket = randomToken(32);
            writeTicket(BIND_TICKET_PREFIX, ticket, outcome);
            return UriComponentsBuilder.fromUriString(frontendReturnUri)
                    .queryParam("oauth_bind_ticket", ticket)
                    .build(true)
                    .toUri();
        }

        AuthResult result = authApplicationService.loginWithOAuth(provider, code, authorizationState.codeVerifier());
        String ticket = randomToken(32);
        writeTicket(TICKET_PREFIX, ticket, result);
        return UriComponentsBuilder.fromUriString(frontendReturnUri)
                .queryParam("oauth_ticket", ticket)
                .build(true)
                .toUri();
    }

    public AuthResult redeemTicket(String ticket) {
        return readTicket(TICKET_PREFIX, ticket, AuthResult.class);
    }

    /** 兑换"绑定第三方账号"结果票据——同登录票据一样一次性、短 TTL，兑换后立即失效。 */
    public OAuthBindOutcome redeemBindTicket(String ticket) {
        return readTicket(BIND_TICKET_PREFIX, ticket, OAuthBindOutcome.class);
    }

    private void storeState(String provider, String state, AuthorizationState value) {
        try {
            redisTemplate.opsForValue().set(stateKey(provider, state), objectMapper.writeValueAsString(value),
                    STATE_TTL);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not persist OAuth authorization state", exception);
        }
    }

    private AuthorizationState readState(String raw) {
        try {
            return objectMapper.readValue(raw, AuthorizationState.class);
        } catch (JsonProcessingException exception) {
            throw IdentityException.oauthTransactionInvalid();
        }
    }

    private <T> void writeTicket(String prefix, String ticket, T payload) {
        try {
            redisTemplate.opsForValue().set(prefix + ticketDigest(ticket), objectMapper.writeValueAsString(payload),
                    TICKET_TTL);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not create OAuth ticket", exception);
        }
    }

    private <T> T readTicket(String prefix, String ticket, Class<T> type) {
        String payload = redisTemplate.opsForValue().getAndDelete(prefix + ticketDigest(ticket));
        if (payload == null) {
            throw IdentityException.oauthTransactionInvalid();
        }
        try {
            return objectMapper.readValue(payload, type);
        } catch (JsonProcessingException exception) {
            throw IdentityException.oauthTransactionInvalid();
        }
    }

    private OAuthProviderClient provider(String provider) {
        OAuthProviderClient client = providerClients.get(provider.toLowerCase(Locale.ROOT));
        if (client == null) {
            throw IdentityException.oauthProviderNotConfigured(provider);
        }
        return client;
    }

    private String stateKey(String provider, String state) {
        return STATE_PREFIX + provider.toLowerCase(Locale.ROOT) + ":"
                + base64Url(sha256(state.getBytes(StandardCharsets.UTF_8)));
    }

    private String ticketDigest(String ticket) {
        return base64Url(sha256(ticket.getBytes(StandardCharsets.UTF_8)));
    }

    private String randomToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        secureRandom.nextBytes(bytes);
        return base64Url(bytes);
    }

    private byte[] sha256(byte[] value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
