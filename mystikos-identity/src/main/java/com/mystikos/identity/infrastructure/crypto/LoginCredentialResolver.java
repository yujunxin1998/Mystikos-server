package com.mystikos.identity.infrastructure.crypto;

import com.mystikos.identity.application.command.CredentialType;
import com.mystikos.identity.domain.IdentityException;
import org.springframework.stereotype.Component;

/**
 * 把 {@code LoginRequest} 里的凭证字段解析成现有 {@code LoginCommand} 需要的明文密码/验证码，
 * 是 RSA 加密登录接进现有登录流程的唯一衔接点——{@code AuthApplicationService.login()}
 * 本身完全不变，仍然只接收明文 credential。
 *
 * <ul>
 *   <li>验证码登录：不涉及加密，原样透传现有 credential 字段。</li>
 *   <li>密码登录 + login-encryption.enabled=false：仅允许明文 credential（本地开发/紧急回退）。</li>
 *   <li>密码登录 + login-encryption.enabled=true：必须提交 keyId + encryptedCredential，
 *       禁止回退明文，解密后返回原始密码。</li>
 * </ul>
 */
@Component
public class LoginCredentialResolver {

    private final LoginEncryptionProperties properties;
    private final RsaLoginCredentialDecryptor decryptor;

    public LoginCredentialResolver(LoginEncryptionProperties properties, RsaLoginCredentialDecryptor decryptor) {
        this.properties = properties;
        this.decryptor = decryptor;
    }

    public String resolve(CredentialType credentialType, String credential, String keyId, String encryptedCredential) {
        if (credentialType != CredentialType.PASSWORD) {
            return requirePlainCredential(credential);
        }
        if (!properties.isEnabled()) {
            return requirePlainCredential(credential);
        }
        if (isBlank(keyId) || isBlank(encryptedCredential)) {
            throw IdentityException.loginEncryptionRequired();
        }
        return decryptor.decrypt(keyId, encryptedCredential);
    }

    private static String requirePlainCredential(String credential) {
        if (isBlank(credential)) {
            throw IdentityException.credentialInvalid();
        }
        return credential;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
