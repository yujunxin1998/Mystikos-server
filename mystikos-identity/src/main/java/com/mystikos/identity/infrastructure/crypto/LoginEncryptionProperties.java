package com.mystikos.identity.infrastructure.crypto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 登录密码 RSA 加密配置。enabled=false 时仅允许原有明文 credential 登录（本地开发/紧急回退）；
 * enabled=true 时 PASSWORD 登录必须提交 keyId + encryptedCredential，见 {@link LoginCredentialResolver}。
 */
@Data
@ConfigurationProperties(prefix = "mystikos.security.login-encryption")
public class LoginEncryptionProperties {

    private boolean enabled = true;

    private String keyId = "login-key-v1";

    /** X.509 SubjectPublicKeyInfo PEM 文件路径；支持 classpath: 前缀，否则按文件系统路径读取。 */
    private String publicKeyPath;

    /** PKCS8 PEM 私钥文件路径，同上支持 classpath: 前缀。生产环境应指向 Docker volume/secret 挂载路径。 */
    private String privateKeyPath;
}
