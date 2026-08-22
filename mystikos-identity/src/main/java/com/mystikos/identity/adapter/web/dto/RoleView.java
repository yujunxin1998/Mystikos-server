package com.mystikos.identity.adapter.web.dto;

import com.mystikos.identity.domain.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "角色视图")
public class RoleView implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "角色编码")
    private String code;

    @Schema(description = "角色展示名")
    private String displayName;

    public static RoleView from(Role role) {
        RoleView view = new RoleView();
        view.setCode(role.getCode());
        view.setDisplayName(role.getDisplayName());
        return view;
    }
}
