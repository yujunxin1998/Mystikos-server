package com.mystikos.identity.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "第三方登录请求")
public class OAuthLoginRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "第三方授权码")
    @NotBlank
    private String code;
}
