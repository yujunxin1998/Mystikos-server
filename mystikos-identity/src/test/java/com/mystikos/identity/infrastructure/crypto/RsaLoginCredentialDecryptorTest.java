package com.mystikos.identity.infrastructure.crypto;

import com.mystikos.identity.adapter.web.dto.LoginPublicKeyResponse;
import com.mystikos.identity.domain.IdentityException;
import com.mystikos.identity.domain.IdentityResponseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 用跟浏览器 Web Crypto RSA-OAEP 完全一致的参数（OAEP 摘要 SHA-256、MGF1 摘要 SHA-256、
 * 空 label）在 JVM 内加密，模拟前端会产出的密文，验证 RsaLoginCredentialDecryptor 能正确解密——
 * 这是 OAEP-SHA-256/MGF1-SHA256 组合本身标准化、跨实现互通的算法层面证明；
 * 真正跑一遍浏览器 window.crypto.subtle 需要独立的前端联调环境，不在本仓库的 JVM 测试范围内。
 */
class RsaLoginCredentialDecryptorTest {

    private static final String KEY_ID = "login-key-v1";

    private LoginKeyProvider keyProvider;
    private RsaLoginCredentialDecryptor decryptor;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws Exception {
        LoginEncryptionProperties properties = new LoginEncryptionProperties();
        properties.setEnabled(true);
        properties.setKeyId(KEY_ID);
        properties.setPublicKeyPath("classpath:login-keys/test-public-key.pem");
        properties.setPrivateKeyPath("classpath:login-keys/test-private-key.pem");

        keyProvider = new LoginKeyProvider(properties);
        keyProvider.init();
        decryptor = new RsaLoginCredentialDecryptor(keyProvider);

        LoginPublicKeyResponse publicKeyResponse = keyProvider.currentPublicKey();
        publicKey = parsePublicKey(publicKeyResponse.getPublicKey());
    }

    @Test
    void decryptsCiphertextEncryptedWithWebCryptoCompatibleOaepParams() throws Exception {
        String rawPassword = "S3cur3-P@ssw0rd!";
        String encrypted = encrypt(rawPassword);

        String decrypted = decryptor.decrypt(KEY_ID, encrypted);

        assertThat(decrypted).isEqualTo(rawPassword);
    }

    @Test
    void rejectsMismatchedKeyId() throws Exception {
        String encrypted = encrypt("whatever-password");

        assertThatThrownBy(() -> decryptor.decrypt("wrong-key-id", encrypted))
                .isInstanceOfSatisfying(IdentityException.class,
                        e -> assertThat(e.getResultCode()).isEqualTo(IdentityResponseCode.LOGIN_KEY_NOT_FOUND));
    }

    @Test
    void rejectsInvalidBase64() {
        assertThatThrownBy(() -> decryptor.decrypt(KEY_ID, "not-valid-base64-!!!@@@"))
                .isInstanceOfSatisfying(IdentityException.class, e ->
                        assertThat(e.getResultCode()).isEqualTo(IdentityResponseCode.LOGIN_CREDENTIAL_DECRYPTION_FAILED));
    }

    @Test
    void rejectsTamperedCiphertext() throws Exception {
        String encrypted = encrypt("another-password");
        byte[] cipherBytes = Base64.getDecoder().decode(encrypted);
        cipherBytes[cipherBytes.length / 2] ^= 0xFF; // 翻转中间一个字节，模拟密文被篡改
        String tampered = Base64.getEncoder().encodeToString(cipherBytes);

        assertThatThrownBy(() -> decryptor.decrypt(KEY_ID, tampered))
                .isInstanceOfSatisfying(IdentityException.class, e ->
                        assertThat(e.getResultCode()).isEqualTo(IdentityResponseCode.LOGIN_CREDENTIAL_DECRYPTION_FAILED));
    }

    @Test
    void rejectsCiphertextWithWrongBlockLength() {
        // 合法 Base64，但解码后长度跟 2048 位密钥的密文长度（256 字节）对不上。
        String shortCiphertext = Base64.getEncoder().encodeToString("too-short-to-be-a-real-rsa-block".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> decryptor.decrypt(KEY_ID, shortCiphertext))
                .isInstanceOfSatisfying(IdentityException.class, e ->
                        assertThat(e.getResultCode()).isEqualTo(IdentityResponseCode.LOGIN_CREDENTIAL_DECRYPTION_FAILED));
    }

    @Test
    void rejectsOverlongCiphertext() {
        String overlong = "A".repeat(3000);

        assertThatThrownBy(() -> decryptor.decrypt(KEY_ID, overlong))
                .isInstanceOfSatisfying(IdentityException.class, e ->
                        assertThat(e.getResultCode()).isEqualTo(IdentityResponseCode.LOGIN_CREDENTIAL_DECRYPTION_FAILED));
    }

    @Test
    void rejectsBlankKeyIdOrCiphertext() {
        assertThatThrownBy(() -> decryptor.decrypt("", "irrelevant"))
                .isInstanceOfSatisfying(IdentityException.class, e ->
                        assertThat(e.getResultCode()).isEqualTo(IdentityResponseCode.LOGIN_ENCRYPTION_REQUIRED));
        assertThatThrownBy(() -> decryptor.decrypt(KEY_ID, ""))
                .isInstanceOfSatisfying(IdentityException.class, e ->
                        assertThat(e.getResultCode()).isEqualTo(IdentityResponseCode.LOGIN_ENCRYPTION_REQUIRED));
    }

    /** 模拟前端：用后端下发的公钥 + RSA-OAEP/SHA-256/MGF1-SHA256 加密，跟 window.crypto.subtle 的默认行为一致。 */
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
}
