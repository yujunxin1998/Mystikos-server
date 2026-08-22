package com.mystikos.identity.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.identity.application.service.UserApplicationService;
import com.mystikos.identity.domain.model.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * 用户管理态操作（角色分配/封禁/权限查询）。注册/登录属于 S1，见 {@link AuthController}；
 * 老板资料属于 S2，见 {@link ProfileController}。
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户管理", description = "角色分配/封禁/权限查询，运营态操作")
public class UserController {

    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @PostMapping("/{userId}/roles/{role}")
    @Operation(summary = "分配角色")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Void> assignRole(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "角色") @PathVariable Role role) {
        userApplicationService.assignRole(userId, role);
        return APIResponse.ok();
    }

    @DeleteMapping("/{userId}/roles/{role}")
    @Operation(summary = "移除角色", description = "不能移除用户的最后一个角色")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Void> removeRole(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "角色") @PathVariable Role role) {
        userApplicationService.removeRole(userId, role);
        return APIResponse.ok();
    }

    @PostMapping("/{userId}/ban")
    @Operation(summary = "封禁用户")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Void> ban(@Parameter(description = "用户ID") @PathVariable Long userId) {
        userApplicationService.ban(userId);
        return APIResponse.ok();
    }

    @GetMapping("/{userId}/permissions")
    @Operation(summary = "查询用户已解析的权限编码集合", description = "拥有 ADMIN 角色时返回通配符 \"*\"")
    public APIResponse<Set<String>> permissions(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return APIResponse.ok(userApplicationService.resolvePermissions(userId));
    }
}
