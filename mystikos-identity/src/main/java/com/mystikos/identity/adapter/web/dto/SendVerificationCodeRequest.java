package com.mystikos.identity.adapter.web.dto;

import com.mystikos.identity.domain.model.AuthChannel;
import com.mystikos.identity.domain.model.VerificationPurpose;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "发送验证码请求")
public class SendVerificationCodeRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "渠道：PHONE/EMAIL")
    @NotNull
    private AuthChannel channel;

    @Schema(description = "手机号或邮箱")
    @NotBlank
    private String identifier;

    @Schema(description = "用途：REGISTER/LOGIN/RESET_PASSWORD；BIND_CONTACT 仅由登录后的资料接口使用")
    @NotNull
    private VerificationPurpose purpose;
}
