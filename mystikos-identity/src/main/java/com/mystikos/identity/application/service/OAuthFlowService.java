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

/** Manages server-side OAuth state, PKCE and short-lived one-time login tickets. */
@Service
public class OAuthFlowService {

    private static final Duration STATE_TTL = Duration.ofMinutes(5);
    private static final Duration TICKET_TTL = Duration.ofMinutes(1);
    private static final String STATE_PREFIX = "identity:oauth:state:";
    private static final String TICKET_PREFIX = "identity:oauth:ticket:";

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
        redisTemplate.opsForValue().set(stateKey(provider, state), codeVerifier, STATE_TTL);
        return client.buildAuthorizationUri(state, codeChallenge);
    }

    public URI completeAuthorization(String provider, String code, String state) {
        if (frontendReturnUri == null || frontendReturnUri.isBlank()) {
            throw new IllegalStateException("OAUTH_FRONTEND_RETURN_URI is required for OAuth login");
        }
        String codeVerifier = redisTemplate.opsForValue().getAndDelete(stateKey(provider, state));
        if (codeVerifier == null) {
            throw IdentityException.oauthTransactionInvalid();
        }
        AuthResult result = authApplicationService.loginWithOAuth(provider, code, codeVerifier);
        String ticket = randomToken(32);
        try {
            redisTemplate.opsForValue().set(ticketKey(ticket), objectMapper.writeValueAsString(result), TICKET_TTL);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not create OAuth login ticket", exception);
        }
        return UriComponentsBuilder.fromUriString(frontendReturnUri)
                .queryParam("oauth_ticket", ticket)
                .build(true)
                .toUri();
    }

    public AuthResult redeemTicket(String ticket) {
        String payload = redisTemplate.opsForValue().getAndDelete(ticketKey(ticket));
        if (payload == null) {
            throw IdentityException.oauthTransactionInvalid();
        }
        try {
            return objectMapper.readValue(payload, AuthResult.class);
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

    private String ticketKey(String ticket) {
        return TICKET_PREFIX + base64Url(sha256(ticket.getBytes(StandardCharsets.UTF_8)));
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
