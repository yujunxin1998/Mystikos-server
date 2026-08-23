package com.mystikos.identity.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.common.result.PageResult;
import com.mystikos.identity.adapter.web.dto.AdminCreateUserRequest;
import com.mystikos.identity.application.service.UserApplicationService;
import com.mystikos.identity.application.service.UserProfileView;
import com.mystikos.identity.domain.model.Role;
import com.mystikos.identity.domain.model.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * 后台管理接口：用户管理态操作（新增/删除/角色分配/封禁/权限查询/列表检索）。
 * 归入 {@code /api/v1/manage/**} 路由前缀，与其他后台管理接口统一收口，方便后续权限控制。
 * 注册/登录属于 S1，见 {@link AuthController}；老板资料属于 S2，见 {@link ProfileController}。
 */
@RestController
@RequestMapping("/api/v1/manage/users")
@Tag(name = "后台管理 - 用户管理", description = "新增/删除/角色分配/封禁/权限查询/列表检索，运营态操作")
public class UserController {

    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @PostMapping
    @Operation(summary = "管理员新增用户", description = "跳过验证码校验，手机号/邮箱唯一性与注册流程一致")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<UserProfileView> create(@Valid @RequestBody AdminCreateUserRequest request) {
        return APIResponse.ok(userApplicationService.createUser(request));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户", description = "逻辑删除，账号状态置为 DELETED，不做物理删除")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Void> delete(@Parameter(description = "用户ID") @PathVariable Long userId) {
        userApplicationService.deleteUser(userId);
        return APIResponse.ok();
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

    @GetMapping
    @Operation(summary = "分页查询用户列表",
            description = "按创建时间倒序，支持按账号状态、创建时间范围、关键字（昵称/手机号/邮箱）过滤，运营态操作")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<PageResult<UserProfileView>> list(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "账号状态") @RequestParam(required = false) UserStatus status,
            @Parameter(description = "创建时间范围-起（ISO-8601）")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdFrom,
            @Parameter(description = "创建时间范围-止（ISO-8601）")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdTo,
            @Parameter(description = "关键字，匹配昵称/手机号/邮箱") @RequestParam(required = false) String keyword) {
        return APIResponse.ok(
                userApplicationService.listUsers(pageNum, pageSize, status, createdFrom, createdTo, keyword));
    }
}
