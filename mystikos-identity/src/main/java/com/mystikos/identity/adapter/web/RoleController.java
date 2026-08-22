package com.mystikos.identity.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.identity.adapter.web.dto.RoleView;
import com.mystikos.identity.domain.model.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 角色列表，只读。数据源是 {@link Role} 枚举，不查数据库——
 * identity_role 表只用来给 identity_user_role/identity_role_permission
 * 提供外键完整性，展示名/排序仍然以枚举为准，避免两处数据漂移。
 */
@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "用户管理", description = "角色分配/封禁/权限查询，运营态操作")
public class RoleController {

    @GetMapping
    @Operation(summary = "查询角色列表", description = "固定 6 个角色，按枚举声明顺序返回")
    public APIResponse<List<RoleView>> list() {
        return APIResponse.ok(Arrays.stream(Role.values()).map(RoleView::from).toList());
    }
}
