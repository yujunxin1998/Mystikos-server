package com.mystikos.identity.adapter.web.dto;

import com.mystikos.identity.application.command.CredentialType;
import com.mystikos.identity.domain.model.AuthChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "登录请求")
public class LoginRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "渠道：PHONE/EMAIL")
    @NotNull
    private AuthChannel channel;

    @Schema(description = "手机号或邮箱")
    @NotBlank
    private String identifier;

    @Schema(description = "凭证类型：PASSWORD/VERIFICATION_CODE")
    @NotNull
    private CredentialType credentialType;

    /**
     * 验证码登录必填；密码登录仅在未启用登录加密（login-encryption.enabled=false）时使用，
     * 启用加密后密码登录必须走 encryptedCredential，这里不再校验 @NotBlank——
     * 具体是否必填由 LoginCredentialResolver 按 credentialType + 加密开关分支校验。
     */
    @ToString.Exclude
    @Schema(description = "密码或验证码；验证码登录必填，密码登录仅在未启用登录加密时使用明文密码")
    private String credential;

    @Schema(description = "登录公钥版本号，取自 GET /api/v1/auth/public-key 返回的 keyId；密码登录启用加密时必填")
    private String keyId;

    @Size(max = 2048)
    @Schema(description = "Base64 编码的 RSA-OAEP-256 加密密码；密码登录启用加密时必填")
    private String encryptedCredential;
}
