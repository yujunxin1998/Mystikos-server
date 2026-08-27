package com.mystikos.identity.infrastructure.crypto;

import com.mystikos.identity.adapter.web.dto.LoginPublicKeyResponse;
import com.mystikos.identity.domain.IdentityException;
import com.mystikos.identity.domain.IdentityResponseCode;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginKeyProviderTest {

    private static final String KEY_ID = "login-key-v1";

    private LoginEncryptionProperties propertiesWithTestKeys(boolean enabled) {
        LoginEncryptionProperties properties = new LoginEncryptionProperties();
        properties.setEnabled(enabled);
        properties.setKeyId(KEY_ID);
        properties.setPublicKeyPath("classpath:login-keys/test-public-key.pem");
        properties.setPrivateKeyPath("classpath:login-keys/test-private-key.pem");
        return properties;
    }

    @Test
    void loadsConfiguredKeyPairAndExposesPublicKeyOnly() {
        LoginKeyProvider provider = new LoginKeyProvider(propertiesWithTestKeys(true));
        provider.init();

        LoginPublicKeyResponse response = provider.currentPublicKey();

        assertThat(response.getKeyId()).isEqualTo(KEY_ID);
        assertThat(response.getAlgorithm()).isEqualTo("RSA-OAEP-256");
        assertThat(response.getPublicKey()).contains("BEGIN PUBLIC KEY");
        // 私钥内容任何情况下都不应该出现在对外暴露的响应对象里。
        assertThat(response.getPublicKey()).doesNotContain("PRIVATE KEY");
    }

    @Test
    void requireActiveKeyRejectsMismatchedKeyId() {
        LoginKeyProvider provider = new LoginKeyProvider(propertiesWithTestKeys(true));
        provider.init();

        assertThatThrownBy(() -> provider.requireActiveKey("some-other-key-id"))
                .isInstanceOfSatisfying(IdentityException.class,
                        e -> assertThat(e.getResultCode()).isEqualTo(IdentityResponseCode.LOGIN_KEY_NOT_FOUND));
    }

    @Test
    void enabledTrueWithoutKeyPathsFailsFastAtStartup() {
        LoginEncryptionProperties properties = new LoginEncryptionProperties();
        properties.setEnabled(true);
        // publicKeyPath/privateKeyPath 留空 —— enabled=true 时必须配置密钥，配置缺失应启动失败。

        LoginKeyProvider provider = new LoginKeyProvider(properties);

        assertThatThrownBy(provider::init).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void disabledWithoutKeyPathsLeavesPublicKeyUnconfigured() {
        LoginEncryptionProperties properties = new LoginEncryptionProperties();
        properties.setEnabled(false);
        // 本地开发/紧急回退：没配密钥路径不应该阻塞启动。

        LoginKeyProvider provider = new LoginKeyProvider(properties);
        provider.init();

        assertThatThrownBy(provider::currentPublicKey)
                .isInstanceOfSatisfying(IdentityException.class, e ->
                        assertThat(e.getResultCode()).isEqualTo(IdentityResponseCode.LOGIN_ENCRYPTION_NOT_CONFIGURED));
    }

    @Test
    void rejectsRsaKeyShorterThan2048Bits(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        KeyPair weakKeyPair = generator.generateKeyPair();

        java.nio.file.Path publicPath = tempDir.resolve("weak-public.pem");
        java.nio.file.Path privatePath = tempDir.resolve("weak-private.pem");
        java.nio.file.Files.writeString(publicPath, toPem("PUBLIC KEY", weakKeyPair.getPublic().getEncoded()));
        java.nio.file.Files.writeString(privatePath, toPem("PRIVATE KEY", weakKeyPair.getPrivate().getEncoded()));

        LoginEncryptionProperties properties = new LoginEncryptionProperties();
        properties.setEnabled(true);
        properties.setKeyId(KEY_ID);
        properties.setPublicKeyPath(publicPath.toString());
        properties.setPrivateKeyPath(privatePath.toString());

        LoginKeyProvider provider = new LoginKeyProvider(properties);

        assertThatThrownBy(provider::init).isInstanceOf(IllegalStateException.class);
    }

    private static String toPem(String label, byte[] der) {
        String base64 = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(der);
        return "-----BEGIN " + label + "-----\n" + base64 + "\n-----END " + label + "-----\n";
    }
}
