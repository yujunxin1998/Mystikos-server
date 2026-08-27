package com.mystikos.identity.infrastructure.crypto;

import com.mystikos.identity.domain.IdentityException;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.spec.MGF1ParameterSpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * 登录密码的 RSA-OAEP-256 解密。Base64 解码、密文长度校验、Cipher 解密全部封装在这里，
 * 任何失败路径一律折叠成 {@link IdentityException#loginCredentialDecryptionFailed()}，
 * 不把 Base64 格式错误/密文被篡改/私钥解密失败这几种情况区分暴露给客户端。
 */
@Component
public class RsaLoginCredentialDecryptor {

    /**
     * Base64 文本长度上限，早于密钥加载/解码之前就能拦掉异常大请求。
     * 4096-bit RSA 密文是 512 字节，Base64 后约 684 字符，这里留足冗余，同时远小于能拖垮服务的体量。
     */
    private static final int MAX_ENCODED_LENGTH = 2048;

    private static final String CIPHER_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    private final LoginKeyProvider keyProvider;

    public RsaLoginCredentialDecryptor(LoginKeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    /** 返回解密后的原始密码；调用方应尽快用完并清理引用，不落缓存、不落库、不打日志。 */
    public String decrypt(String keyId, String encryptedCredentialBase64) {
        if (isBlank(keyId) || isBlank(encryptedCredentialBase64)) {
            throw IdentityException.loginEncryptionRequired();
        }
        if (encryptedCredentialBase64.length() > MAX_ENCODED_LENGTH) {
            throw IdentityException.loginCredentialDecryptionFailed();
        }

        // keyId 不匹配（含密钥轮换后的旧版本号）在这里就会抛 loginKeyNotFound，不会走到实际解密。
        LoginKeyMaterial keyMaterial = keyProvider.requireActiveKey(keyId);

        byte[] cipherBytes;
        try {
            cipherBytes = Base64.getDecoder().decode(encryptedCredentialBase64);
        } catch (IllegalArgumentException e) {
            throw IdentityException.loginCredentialDecryptionFailed();
        }

        if (cipherBytes.length != keyMaterial.modulusByteLength()) {
            throw IdentityException.loginCredentialDecryptionFailed();
        }

        byte[] plainBytes = null;
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            OAEPParameterSpec oaepParameterSpec = new OAEPParameterSpec(
                    "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, keyMaterial.privateKey(), oaepParameterSpec);
            plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            // Base64 篡改导致的分组损坏、密文被篡改、私钥不匹配等都会落到这里（常见是 BadPaddingException），
            // 统一折叠成同一个业务错误，不回显 Cipher 的原始异常信息。
            throw IdentityException.loginCredentialDecryptionFailed();
        } finally {
            Arrays.fill(cipherBytes, (byte) 0);
            if (plainBytes != null) {
                Arrays.fill(plainBytes, (byte) 0);
            }
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
