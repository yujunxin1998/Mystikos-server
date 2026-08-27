package com.mystikos.identity.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mystikos.common.web.exception.GlobalExceptionHandler;
import com.mystikos.identity.application.command.CredentialType;
import com.mystikos.identity.application.command.LoginCommand;
import com.mystikos.identity.application.service.AuthApplicationService;
import com.mystikos.identity.application.service.AuthResult;
import com.mystikos.identity.application.service.OAuthFlowService;
import com.mystikos.identity.application.service.UserApplicationService;
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

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * login-encryption.enabled=false：本地开发/紧急回退场景，密钥路径可以完全不配置，
 * 密码登录应继续按原有明文 credential 流程工作，不受这次改动影响。
 */
class AuthControllerLoginEncryptionDisabledTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private AuthApplicationService authApplicationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LoginEncryptionProperties properties = new LoginEncryptionProperties();
        properties.setEnabled(false);
        // publicKeyPath/privateKeyPath 留空：enabled=false 时允许完全不配置密钥。

        LoginKeyProvider keyProvider = new LoginKeyProvider(properties);
        keyProvider.init();
        LoginCredentialResolver resolver = new LoginCredentialResolver(properties, new RsaLoginCredentialDecryptor(keyProvider));

        authApplicationService = mock(AuthApplicationService.class);
        AuthController controller = new AuthController(authApplicationService, mock(UserApplicationService.class),
                mock(OAuthFlowService.class), keyProvider, resolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void plainPasswordLoginStillWorksWhenEncryptionDisabled() throws Exception {
        when(authApplicationService.login(any())).thenReturn(new AuthResult("access-token", "refresh-token", 1L));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("channel", AuthChannel.EMAIL.name());
        payload.put("identifier", "user@example.com");
        payload.put("credentialType", CredentialType.PASSWORD.name());
        payload.put("credential", "plain-password");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        ArgumentCaptor<LoginCommand> captor = ArgumentCaptor.forClass(LoginCommand.class);
        verify(authApplicationService).login(captor.capture());
        assertThat(captor.getValue().credential()).isEqualTo("plain-password");
    }
}
