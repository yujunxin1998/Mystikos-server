package com.mystikos.identity.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mystikos.common.web.exception.GlobalExceptionHandler;
import com.mystikos.identity.application.command.CredentialType;
import com.mystikos.identity.application.command.LoginCommand;
import com.mystikos.identity.application.service.AuthApplicationService;
import com.mystikos.identity.application.service.AuthResult;
import com.mystikos.identity.application.service.OAuthFlowService;
import com.mystikos.identity.application.service.UserApplicationService;
import com.mystikos.identity.domain.IdentityException;
import com.mystikos.identity.domain.model.AuthChannel;
import com.mystikos.identity.infrastructure.crypto.LoginCredentialResolver;
import com.mystikos.identity.infrastructure.crypto.LoginEncryptionProperties;
import com.mystikos.identity.infrastructure.crypto.LoginKeyProvider;
import com.mystikos.identity.infrastructure.crypto.RsaLoginCredentialDecryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc standalone（不起 Spring 容器，不接 DB/Redis）覆盖 RSA 登录加密在 AuthController 上的
 * 全部分支。AuthApplicationService 用 Mockito mock 顶替——真正的 BCrypt 校验/限流/JWT 签发本来就
 * 完全不受这次改动影响（login() 方法一行没改），这里只验证 LoginCredentialResolver 能正确把
 * 明文/密文归一成传给它的 credential，以及各种非法输入在到达它之前就被拦下。
 */
class AuthControllerLoginEncryptionTest {

    private static final String KEY_ID = "login-key-v1";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private AuthApplicationService authApplicationService;
    private MockMvc mockMvc;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws Exception {
        LoginEncryptionProperties properties = new LoginEncryptionProperties();
        properties.setEnabled(true);
        properties.setKeyId(KEY_ID);
        properties.setPublicKeyPath("classpath:login-keys/test-public-key.pem");
        properties.setPrivateKeyPath("classpath:login-keys/test-private-key.pem");

        LoginKeyProvider keyProvider = new LoginKeyProvider(properties);
        keyProvider.init();
        LoginCredentialResolver resolver = new LoginCredentialResolver(properties, new RsaLoginCredentialDecryptor(keyProvider));

        authApplicationService = mock(AuthApplicationService.class);
        UserApplicationService userApplicationService = mock(UserApplicationService.class);
        OAuthFlowService oauthFlowService = mock(OAuthFlowService.class);

        AuthController controller = new AuthController(authApplicationService, userApplicationService,
                oauthFlowService, keyProvider, resolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        publicKey = parsePublicKey(keyProvider.currentPublicKey().getPublicKey());
    }

    @Test
    void publicKeyEndpointReturnsKeyWithoutPrivateMaterial() throws Exception {
        String body = mockMvc.perform(get("/api/v1/auth/public-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.keyId").value(KEY_ID))
                .andExpect(jsonPath("$.data.algorithm").value("RSA-OAEP-256"))
                .andExpect(jsonPath("$.data.publicKey").value(org.hamcrest.Matchers.containsString("BEGIN PUBLIC KEY")))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(body).doesNotContain("PRIVATE KEY");
    }

    @Test
    void loginWithCorrectlyEncryptedPasswordReachesExistingServiceWithDecryptedPlaintext() throws Exception {
        String rawPassword = "S3cur3-P@ssw0rd!";
        when(authApplicationService.login(any())).thenReturn(new AuthResult("access-token", "refresh-token", 1L));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson(AuthChannel.EMAIL, "user@example.com", CredentialType.PASSWORD,
                                null, KEY_ID, encrypt(rawPassword))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));

        ArgumentCaptor<LoginCommand> captor = ArgumentCaptor.forClass(LoginCommand.class);
        verify(authApplicationService).login(captor.capture());
        assertThat(captor.getValue().credential()).isEqualTo(rawPassword);
        assertThat(captor.getValue().channel()).isEqualTo(AuthChannel.EMAIL);
        assertThat(captor.getValue().credentialType()).isEqualTo(CredentialType.PASSWORD);
    }

    @Test
    void wrongPasswordAfterCorrectEncryptionSurfacesExistingLoginFailure() throws Exception {
        // 加密流程本身没问题，但底层 AuthApplicationService.login() 判定密码错误——
        // 必须原样透出已有的 CREDENTIAL_INVALID，而不是被 RSA 相关错误码掩盖。
        when(authApplicationService.login(any())).thenThrow(IdentityException.credentialInvalid());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson(AuthChannel.EMAIL, "user@example.com", CredentialType.PASSWORD,
                                null, KEY_ID, encrypt("wrong-password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2010));
    }

    @Test
    void wrongKeyIdIsRejectedBeforeReachingLoginService() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson(AuthChannel.EMAIL, "user@example.com", CredentialType.PASSWORD,
                                null, "some-other-key-id", encrypt("whatever"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2044));

        verifyNoInteractions(authApplicationService);
    }

    @Test
    void invalidBase64IsRejectedBeforeReachingLoginService() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson(AuthChannel.EMAIL, "user@example.com", CredentialType.PASSWORD,
                                null, KEY_ID, "not-valid-base64-!!!@@@")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2045));

        verifyNoInteractions(authApplicationService);
    }

    @Test
    void tamperedCiphertextIsRejectedBeforeReachingLoginService() throws Exception {
        String encrypted = encrypt("some-password");
        byte[] cipherBytes = Base64.getDecoder().decode(encrypted);
        cipherBytes[0] ^= 0xFF;
        String tampered = Base64.getEncoder().encodeToString(cipherBytes);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson(AuthChannel.EMAIL, "user@example.com", CredentialType.PASSWORD,
                                null, KEY_ID, tampered)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2045));

        verifyNoInteractions(authApplicationService);
    }

    @Test
    void overlongCiphertextIsRejectedByBeanValidationBeforeReachingLoginService() throws Exception {
        String overlong = "A".repeat(3000);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson(AuthChannel.EMAIL, "user@example.com", CredentialType.PASSWORD,
                                null, KEY_ID, overlong)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        verifyNoInteractions(authApplicationService);
    }

    @Test
    void plainCredentialFallbackIsRejectedWhenEncryptionEnabled() throws Exception {
        // 只传明文 credential，没有 keyId/encryptedCredential —— enabled=true 时必须拒绝，不能悄悄放行。
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson(AuthChannel.EMAIL, "user@example.com", CredentialType.PASSWORD,
                                "plain-password", null, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(2043));

        verifyNoInteractions(authApplicationService);
    }

    @Test
    void verificationCodeLoginStaysCompatibleAndIgnoresRsaFields() throws Exception {
        when(authApplicationService.login(any())).thenReturn(new AuthResult("access-token", "refresh-token", 1L));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson(AuthChannel.EMAIL, "user@example.com",
                                CredentialType.VERIFICATION_CODE, "123456", null, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        ArgumentCaptor<LoginCommand> captor = ArgumentCaptor.forClass(LoginCommand.class);
        verify(authApplicationService).login(captor.capture());
        assertThat(captor.getValue().credential()).isEqualTo("123456");
        assertThat(captor.getValue().credentialType()).isEqualTo(CredentialType.VERIFICATION_CODE);
    }

    private String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        OAEPParameterSpec spec = new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, spec);
        byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(cipherBytes);
    }

    private static PublicKey parsePublicKey(String pem) throws Exception {
        String base64 = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(base64);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
    }

    private static String loginRequestJson(AuthChannel channel, String identifier, CredentialType credentialType,
                                            String credential, String keyId, String encryptedCredential) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("channel", channel.name());
        payload.put("identifier", identifier);
        payload.put("credentialType", credentialType.name());
        payload.put("credential", credential);
        payload.put("keyId", keyId);
        payload.put("encryptedCredential", encryptedCredential);
        return objectMapper.writeValueAsString(payload);
    }
}
