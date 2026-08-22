package com.mystikos.identity.adapter.web.dto;

import com.mystikos.identity.application.command.CredentialType;
import com.mystikos.identity.domain.model.AuthChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

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

    @Schema(description = "密码或验证码")
    @NotBlank
    private String credential;
}
