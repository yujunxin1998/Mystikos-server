package com.mystikos.identity.adapter.web.dto;

import com.mystikos.identity.domain.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "管理员新增用户请求")
public class AdminCreateUserRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "手机号，与邮箱二选一（至少填一项）")
    private String phone;

    @Schema(description = "邮箱，与手机号二选一（至少填一项）")
    private String email;

    @Schema(description = "密码，为空表示不设置密码（只能用验证码登录）")
    private String password;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "初始角色")
    @NotNull
    private Role initialRole;
}
