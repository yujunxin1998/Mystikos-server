package com.mystikos.identity.application.service;

import com.mystikos.common.cache.RateLimiter;
import com.mystikos.common.event.DomainEventPublisher;
import com.mystikos.common.security.JwtTokenService;
import com.mystikos.identity.application.command.CredentialType;
import com.mystikos.identity.application.command.LoginCommand;
import com.mystikos.identity.application.command.RegisterCommand;
import com.mystikos.identity.application.port.OAuthProviderClient;
import com.mystikos.identity.application.port.OAuthUserInfo;
import com.mystikos.identity.application.port.VerificationCodeSender;
import com.mystikos.identity.domain.IdentityException;
import com.mystikos.identity.domain.event.UserRegisteredEvent;
import com.mystikos.identity.domain.model.AuthChannel;
import com.mystikos.identity.domain.model.OAuthBinding;
import com.mystikos.identity.domain.model.RefreshToken;
import com.mystikos.identity.domain.model.Role;
import com.mystikos.identity.domain.model.User;
import com.mystikos.identity.domain.model.UserStatus;
import com.mystikos.identity.domain.model.VerificationCode;
import com.mystikos.identity.domain.model.VerificationPurpose;
import com.mystikos.identity.domain.repository.RefreshTokenRepository;
import com.mystikos.identity.domain.repository.UserRepository;
import com.mystikos.identity.domain.repository.VerificationCodeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * S1 账号与认证：验证码、注册、登录（密码/验证码二选一）、第三方登录（Discord 已接入，其他 Provider 占位）、
 * Token 刷新与登出。手机号/邮箱登录后签发的都是我们自己的 JWT，见 mystikos-common-security。
 */
@Service
public class AuthApplicationService {

    private static final Duration VERIFICATION_CODE_TTL = Duration.ofMinutes(5);
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

    /** 同一 channel+identifier 重新发验证码的冷却时间，不区分 purpose——不然换个 purpose 就能绕开限流。 */
    private static final Duration VERIFICATION_CODE_RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final int LOGIN_MAX_ATTEMPTS = 10;
    private static final Duration LOGIN_RATE_LIMIT_WINDOW = Duration.ofMinutes(15);

    /** 同一 channel+identifier 验证码校验的失败/尝试次数上限，防止 6 位码被短时间内枚举猜中。 */
    private static final int VERIFICATION_CODE_MAX_ATTEMPTS = 10;
    private static final Duration VERIFICATION_CODE_ATTEMPT_WINDOW = Duration.ofMinutes(5);

    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final VerificationCodeSender verificationCodeSender;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final DomainEventPublisher eventPublisher;
    private final RateLimiter rateLimiter;
    private final Map<String, OAuthProviderClient> oauthProviderClients;
    private final SecureRandom random = new SecureRandom();

    public AuthApplicationService(UserRepository userRepository,
                                   VerificationCodeRepository verificationCodeRepository,
                                   VerificationCodeSender verificationCodeSender,
                                   RefreshTokenRepository refreshTokenRepository,
                                   PasswordEncoder passwordEncoder,
                                   JwtTokenService jwtTokenService,
                                   DomainEventPublisher eventPublisher,
                                   RateLimiter rateLimiter,
                                   List<OAuthProviderClient> oauthProviderClients) {
        this.userRepository = userRepository;
        this.verificationCodeRepository = verificationCodeRepository;
        this.verificationCodeSender = verificationCodeSender;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.eventPublisher = eventPublisher;
        this.rateLimiter = rateLimiter;
        this.oauthProviderClients = oauthProviderClients.stream()
                .collect(Collectors.toMap(c -> c.providerCode().toLowerCase(), Function.identity()));
    }

    @Transactional
    public void sendVerificationCode(AuthChannel channel, String identifier, VerificationPurpose purpose) {
        String rateLimitKey = "identity:ratelimit:vcode:" + channel.name() + ":" + identifier;
        if (!rateLimiter.tryAcquire(rateLimitKey, 1, VERIFICATION_CODE_RESEND_COOLDOWN)) {
            throw IdentityException.verificationCodeRateLimited();
        }
        String code = String.format("%06d", random.nextInt(1_000_000));
        VerificationCode verificationCode = VerificationCode.issue(channel, identifier, purpose, code,
                VERIFICATION_CODE_TTL);
        verificationCodeRepository.save(verificationCode);
        verificationCodeSender.send(channel, identifier, code, purpose);
    }

    /** 向当前用户准备绑定的邮箱或手机号发送验证码。 */
    @Transactional
    public void sendContactVerificationCode(Long userId, AuthChannel channel, String identifier) {
        getUser(userId);
        String normalized = normalizeIdentifier(channel, identifier);
        ensureIdentifierAvailable(userId, channel, normalized);
        sendVerificationCode(channel, normalized, VerificationPurpose.BIND_CONTACT);
    }

    /** 消费验证码并把邮箱或手机号绑定到当前用户。 */
    @Transactional
    public void bindVerifiedContact(Long userId, AuthChannel channel, String identifier, String code) {
        User user = getUser(userId);
        String normalized = normalizeIdentifier(channel, identifier);
        ensureIdentifierAvailable(userId, channel, normalized);
        consumeVerificationCode(channel, normalized, VerificationPurpose.BIND_CONTACT, code);
        user.bindContact(channel, normalized);
        userRepository.save(user);
    }

    /**
     * 向当前用户已有的邮箱或手机号发送验证码，作为绑定/换绑/解绑第三方账号前的二次确认。
     * 故意不接受调用方传入的联系方式——必须发到用户自己名下已核实的邮箱/手机号，
     * 否则这道二次确认形同虚设（攻击者拿到会话后可以随便填一个自己的邮箱收码）。
     */
    @Transactional
    public void sendOAuthBindingVerificationCode(Long userId) {
        User user = getUser(userId);
        SensitiveChannel channel = resolveSensitiveChannel(user);
        sendVerificationCode(channel.channel(), channel.identifier(), VerificationPurpose.OAUTH_BINDING_CHANGE);
    }

    /** 校验绑定/解绑第三方账号前的二次确认验证码；只消费，不做后续动作，供 bind/unbind 复用。 */
    @Transactional
    public void consumeOAuthBindingVerificationCode(Long userId, String verificationCode) {
        User user = getUser(userId);
        SensitiveChannel channel = resolveSensitiveChannel(user);
        consumeVerificationCode(channel.channel(), channel.identifier(), VerificationPurpose.OAUTH_BINDING_CHANGE,
                verificationCode);
    }

    /**
     * 把第三方账号绑定到当前登录用户；同一 provider 已有绑定会被替换（即换绑）。
     * 二次确认验证码由调用方在发起 OAuth 授权前先校验过（见 OAuthFlowService.beginBindAuthorization），
     * 这里只负责拿 code 换用户信息、检查目标第三方账号有没有被别人占用、落库。
     */
    @Transactional
    public OAuthBindOutcome bindOAuthProvider(Long userId, String provider, String authorizationCode,
                                               String codeVerifier) {
        OAuthProviderClient client = oauthProviderClients.get(provider.toLowerCase(Locale.ROOT));
        if (client == null) {
            throw IdentityException.oauthProviderNotConfigured(provider);
        }
        OAuthUserInfo info = client.exchangeCodeForUser(authorizationCode, codeVerifier);

        userRepository.findByOAuthBinding(client.providerCode(), info.providerUserId())
                .filter(owner -> !owner.getId().equals(userId))
                .ifPresent(owner -> { throw IdentityException.oauthAlreadyBoundToAnotherUser(client.providerCode()); });

        User user = getUser(userId);
        user.bindOAuth(new OAuthBinding(client.providerCode(), info.providerUserId(), OffsetDateTime.now()));
        userRepository.save(user);
        return new OAuthBindOutcome(client.providerCode(), info.providerUserId(), info.displayName());
    }

    /**
     * 解绑第三方账号。要求先校验二次确认验证码；解绑后账号必须仍保留至少一种登录方式
     * （手机号/邮箱/其他第三方绑定之一），这条不变量由 {@link User#unbindOAuth} 强制保证。
     */
    @Transactional
    public void unbindOAuthProvider(Long userId, String provider, String verificationCode) {
        consumeOAuthBindingVerificationCode(userId, verificationCode);
        User user = getUser(userId);
        user.unbindOAuth(provider);
        userRepository.save(user);
    }

    private SensitiveChannel resolveSensitiveChannel(User user) {
        if (user.getEmail() != null) {
            return new SensitiveChannel(AuthChannel.EMAIL, user.getEmail());
        }
        if (user.getPhone() != null) {
            return new SensitiveChannel(AuthChannel.PHONE, user.getPhone());
        }
        throw IdentityException.sensitiveOperationContactRequired();
    }

    private record SensitiveChannel(AuthChannel channel, String identifier) {
    }

    @Transactional
    public AuthResult register(RegisterCommand command) {
        consumeVerificationCode(command.channel(), command.identifier(), VerificationPurpose.REGISTER,
                command.verificationCode());

        boolean exists = command.channel() == AuthChannel.PHONE
                ? userRepository.existsByPhone(command.identifier())
                : userRepository.existsByEmail(command.identifier());
        if (exists) {
            throw IdentityException.identifierAlreadyExists(command.identifier());
        }

        String passwordHash = command.rawPassword() != null
                ? passwordEncoder.encode(command.rawPassword())
                : null;
        User user = command.channel() == AuthChannel.PHONE
                ? User.register(command.identifier(), null, passwordHash, command.initialRole())
                : User.register(null, command.identifier(), passwordHash, command.initialRole());

        User saved = userRepository.save(user);
        eventPublisher.publish(new UserRegisteredEvent(saved.getId(), command.identifier()));
        return issueTokens(saved);
    }

    @Transactional
    public AuthResult login(LoginCommand command) {
        String rateLimitKey = "identity:ratelimit:login:" + command.channel().name() + ":" + command.identifier();
        if (!rateLimiter.tryAcquire(rateLimitKey, LOGIN_MAX_ATTEMPTS, LOGIN_RATE_LIMIT_WINDOW)) {
            throw IdentityException.loginRateLimited();
        }

        User user = findByChannel(command.channel(), command.identifier())
                .orElseThrow(IdentityException::credentialInvalid);

        if (command.credentialType() == CredentialType.PASSWORD) {
            if (user.getPasswordHash() == null
                    || !passwordEncoder.matches(command.credential(), user.getPasswordHash())) {
                throw IdentityException.credentialInvalid();
            }
        } else {
            consumeVerificationCode(command.channel(), command.identifier(), VerificationPurpose.LOGIN,
                    command.credential());
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw IdentityException.accountNotActive();
        }
        return issueTokens(user);
    }

    /**
     * 第三方登录：拿授权码换用户信息，本地找不到对应绑定就自动注册一个新用户
     * （{@link User#registerWithOAuth}），角色默认给 MEMBER——走这条路进来的都是消费端用户，
     * 不是陪玩/客服/管理员，那些角色走别的分配渠道。找到已绑定的账号就直接登录。
     */
    @Transactional
    public AuthResult loginWithOAuth(String provider, String authorizationCode) {
        return loginWithOAuth(provider, authorizationCode, null);
    }

    @Transactional
    public AuthResult loginWithOAuth(String provider, String authorizationCode, String codeVerifier) {
        OAuthProviderClient client = oauthProviderClients.get(provider.toLowerCase());
        if (client == null) {
            throw IdentityException.oauthProviderNotConfigured(provider);
        }
        OAuthUserInfo info = client.exchangeCodeForUser(authorizationCode, codeVerifier);
        OAuthBinding binding = new OAuthBinding(client.providerCode(), info.providerUserId(), OffsetDateTime.now());

        Optional<User> existing = userRepository.findByOAuthBinding(client.providerCode(), info.providerUserId());
        User user;
        if (existing.isPresent()) {
            user = existing.get();
        } else {
            user = User.registerWithOAuth(binding, Role.MEMBER);
            if (info.displayName() != null) {
                user.updateProfile(info.displayName());
            }
        }
        User saved = userRepository.save(user);
        if (existing.isEmpty()) {
            eventPublisher.publish(new UserRegisteredEvent(saved.getId(), client.providerCode() + ":" + info.providerUserId()));
        }

        if (saved.getStatus() != UserStatus.ACTIVE) {
            throw IdentityException.accountNotActive();
        }
        return issueTokens(saved);
    }

    @Transactional
    public AuthResult refresh(String rawRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hashToken(rawRefreshToken))
                .orElseThrow(IdentityException::refreshTokenInvalid);
        if (!refreshToken.isValid()) {
            throw IdentityException.refreshTokenInvalid();
        }
        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> IdentityException.notFound(refreshToken.getUserId()));
        String accessToken = jwtTokenService.generateAccessToken(String.valueOf(user.getId()), roleCodes(user));
        return new AuthResult(accessToken, rawRefreshToken, user.getId());
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.revokeAllForUser(userId);
    }

    private AuthResult issueTokens(User user) {
        String accessToken = jwtTokenService.generateAccessToken(String.valueOf(user.getId()), roleCodes(user));
        String rawRefreshToken = generateOpaqueToken();
        RefreshToken refreshToken = RefreshToken.issue(user.getId(), hashToken(rawRefreshToken), REFRESH_TOKEN_TTL);
        refreshTokenRepository.save(refreshToken);
        return new AuthResult(accessToken, rawRefreshToken, user.getId());
    }

    private void consumeVerificationCode(AuthChannel channel, String identifier, VerificationPurpose purpose,
                                          String inputCode) {
        String attemptRateLimitKey = "identity:ratelimit:vcode-attempt:" + channel.name() + ":" + identifier;
        if (!rateLimiter.tryAcquire(attemptRateLimitKey, VERIFICATION_CODE_MAX_ATTEMPTS,
                VERIFICATION_CODE_ATTEMPT_WINDOW)) {
            throw IdentityException.verificationCodeRateLimited();
        }
        VerificationCode verificationCode = verificationCodeRepository
                .findLatestActive(channel, identifier, purpose)
                .orElseThrow(IdentityException::verificationCodeInvalid);
        verificationCode.consume(inputCode);
        verificationCodeRepository.save(verificationCode);
    }

    private Optional<User> findByChannel(AuthChannel channel, String identifier) {
        return channel == AuthChannel.PHONE
                ? userRepository.findByPhone(identifier)
                : userRepository.findByEmail(identifier);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> IdentityException.notFound(userId));
    }

    private void ensureIdentifierAvailable(Long userId, AuthChannel channel, String identifier) {
        findByChannel(channel, identifier)
                .filter(owner -> !owner.getId().equals(userId))
                .ifPresent(owner -> { throw IdentityException.identifierAlreadyExists(identifier); });
    }

    private String normalizeIdentifier(AuthChannel channel, String identifier) {
        String normalized = identifier == null ? "" : identifier.trim();
        return channel == AuthChannel.EMAIL ? normalized.toLowerCase(Locale.ROOT) : normalized;
    }

    private Set<String> roleCodes(User user) {
        return user.getRoles().stream().map(Role::getCode).collect(Collectors.toSet());
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Refresh token 落库只存哈希，泄库也不能直接拿去当 token 用。 */
    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}
