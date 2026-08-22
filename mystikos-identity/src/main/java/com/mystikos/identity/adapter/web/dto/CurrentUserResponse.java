package com.mystikos.identity.adapter.web.dto;

import com.mystikos.identity.application.service.UserProfileView;
import com.mystikos.identity.domain.model.Role;
import com.mystikos.identity.domain.model.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

@Data
@Schema(description = "当前登录用户基础信息")
public class CurrentUserResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "角色集合")
    private Set<Role> roles;

    @Schema(description = "账号状态")
    private UserStatus status;

    public static CurrentUserResponse from(UserProfileView profile) {
        CurrentUserResponse response = new CurrentUserResponse();
        response.setUserId(profile.userId());
        response.setRoles(profile.roles());
        response.setStatus(profile.status());
        return response;
    }
}
