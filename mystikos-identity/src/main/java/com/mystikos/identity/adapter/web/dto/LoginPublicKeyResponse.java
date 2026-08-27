package com.mystikos.identity.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "登录加密公钥")
public class LoginPublicKeyResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "密钥版本，登录时随 encryptedCredential 一并回传")
    private String keyId;

    @Schema(description = "加密算法，固定为 RSA-OAEP-256")
    private String algorithm;

    @Schema(description = "X.509 SubjectPublicKeyInfo 格式的 PEM 公钥")
    private String publicKey;

    public LoginPublicKeyResponse(String keyId, String algorithm, String publicKey) {
        this.keyId = keyId;
        this.algorithm = algorithm;
        this.publicKey = publicKey;
    }
}
