package com.mystikos.identity.adapter.web.dto;

import com.mystikos.identity.domain.model.AuthChannel;
import com.mystikos.identity.domain.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "注册请求")
public class RegisterRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "渠道：PHONE/EMAIL")
    @NotNull
    private AuthChannel channel;

    @Schema(description = "手机号或邮箱")
    @NotBlank
    private String identifier;

    @Schema(description = "验证码")
    @NotBlank
    private String verificationCode;

    @Schema(description = "密码，为空表示不设置密码（只能用验证码登录）")
    private String password;

    @Schema(description = "初始角色")
    @NotNull
    private Role initialRole;
}
