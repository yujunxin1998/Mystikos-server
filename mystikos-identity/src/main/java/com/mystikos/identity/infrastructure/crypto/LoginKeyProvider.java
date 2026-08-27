package com.mystikos.identity.infrastructure.crypto;

import com.mystikos.identity.adapter.web.dto.LoginPublicKeyResponse;
import com.mystikos.identity.domain.IdentityException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 加载并持有当前生效的登录加密 RSA 密钥对。只支持单一当前密钥（keyId 取自配置），
 * 按 keyId 校验请求里的版本号是否匹配，为后续密钥轮换（多 keyId 并存）留出扩展点。
 *
 * <p>enabled=true 时应用启动就必须能加载出合法密钥，加载失败直接抛异常让 Spring 容器启动失败——
 * 不允许"配置错误但服务还是起来了"这种半失败状态。enabled=false 且未配置密钥路径时，
 * 密钥不加载，公钥接口会返回 {@link IdentityException#loginEncryptionNotConfigured()}。
 */
@Component
public class LoginKeyProvider {

    private static final String ALGORITHM_LABEL = "RSA-OAEP-256";
    private static final int MIN_KEY_BITS = 2048;

    private final LoginEncryptionProperties properties;

    private volatile LoginKeyMaterial keyMaterial;

    public LoginKeyProvider(LoginEncryptionProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        boolean pathsConfigured = isNotBlank(properties.getPublicKeyPath()) && isNotBlank(properties.getPrivateKeyPath());

        if (properties.isEnabled() && !pathsConfigured) {
            throw new IllegalStateException(
                    "mystikos.security.login-encryption.enabled=true 时必须配置 "
                            + "mystikos.security.login-encryption.public-key-path 和 private-key-path");
        }
        if (!pathsConfigured) {
            // enabled=false 且没配密钥路径：本地开发/紧急回退场景，公钥接口按未配置处理，不阻塞启动。
            return;
        }
        this.keyMaterial = loadKeyMaterial();
    }

    public LoginPublicKeyResponse currentPublicKey() {
        LoginKeyMaterial material = requireLoaded();
        return new LoginPublicKeyResponse(material.keyId(), ALGORITHM_LABEL, material.publicKeyPem());
    }

    /** 按 keyId 取出当前私钥；keyId 不匹配（包括密钥轮换后旧版本号）一律当"密钥不存在"处理。 */
    LoginKeyMaterial requireActiveKey(String keyId) {
        LoginKeyMaterial material = requireLoaded();
        if (!material.keyId().equals(keyId)) {
            throw IdentityException.loginKeyNotFound();
        }
        return material;
    }

    private LoginKeyMaterial requireLoaded() {
        LoginKeyMaterial material = this.keyMaterial;
        if (material == null) {
            throw IdentityException.loginEncryptionNotConfigured();
        }
        return material;
    }

    private LoginKeyMaterial loadKeyMaterial() {
        try {
            String publicKeyPem = readPemFile(properties.getPublicKeyPath()).trim();
            String privateKeyPem = readPemFile(properties.getPrivateKeyPath()).trim();

            PublicKey publicKey = parsePublicKey(publicKeyPem);
            PrivateKey privateKey = parsePrivateKey(privateKeyPem);

            if (!(publicKey instanceof RSAPublicKey rsaPublicKey)) {
                throw new IllegalStateException("登录加密公钥不是合法的 RSA 公钥");
            }
            if (!(privateKey instanceof RSAPrivateKey rsaPrivateKey)) {
                throw new IllegalStateException("登录加密私钥不是合法的 RSA 私钥");
            }
            int keyBits = rsaPublicKey.getModulus().bitLength();
            if (keyBits < MIN_KEY_BITS) {
                throw new IllegalStateException("登录加密 RSA 密钥长度不足 " + MIN_KEY_BITS + " 位，实际：" + keyBits);
            }
            int modulusByteLength = (keyBits + 7) / 8;

            return new LoginKeyMaterial(properties.getKeyId(), publicKeyPem, rsaPrivateKey, modulusByteLength);
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("登录加密密钥加载失败，请检查密钥文件格式与路径配置", e);
        }
    }

    private PublicKey parsePublicKey(String pem) throws GeneralSecurityException {
        byte[] der = decodePem(pem, "PUBLIC KEY");
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
    }

    private PrivateKey parsePrivateKey(String pem) throws GeneralSecurityException {
        byte[] der = decodePem(pem, "PRIVATE KEY");
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    private byte[] decodePem(String pem, String label) {
        String header = "-----BEGIN " + label + "-----";
        String footer = "-----END " + label + "-----";
        String normalized = pem.replace("\r\n", "\n");
        if (!normalized.contains(header) || !normalized.contains(footer)) {
            throw new IllegalStateException("密钥文件不是合法的 PEM 格式，期望包含 " + header);
        }
        String base64 = normalized.replace(header, "").replace(footer, "").replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }

    private String readPemFile(String location) throws IOException {
        if (location.startsWith("classpath:")) {
            String resourcePath = location.substring("classpath:".length());
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    throw new IOException("classpath 密钥资源不存在：" + resourcePath);
                }
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        return Files.readString(Path.of(location), StandardCharsets.UTF_8);
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
