package com.mystikos.identity.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.common.result.PageResult;
import com.mystikos.identity.adapter.web.dto.AdminCreateCompanionRequest;
import com.mystikos.identity.application.service.CompanionApplicationService;
import com.mystikos.identity.application.service.CompanionStatsView;
import com.mystikos.identity.application.service.CompanionView;
import com.mystikos.identity.domain.model.CompanionStatus;
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

/**
 * 后台管理接口：打手（陪玩）新增/删除/列表检索/统计卡片。
 * 归入 {@code /api/v1/manage/**} 路由前缀，见 {@link UserController} 类注释。
 * "打手"就是拥有 COMPANION 角色的 User，账号本身的启用/封禁/删除走 {@link UserController}。
 */
@RestController
@RequestMapping("/api/v1/manage/companions")
@Tag(name = "后台管理 - 陪玩管理", description = "陪玩新增/删除/列表检索/统计，运营态操作")
public class CompanionController {

    private final CompanionApplicationService companionApplicationService;

    public CompanionController(CompanionApplicationService companionApplicationService) {
        this.companionApplicationService = companionApplicationService;
    }

    @PostMapping
    @Operation(summary = "新增陪玩", description = "创建登录账号（角色=COMPANION）+ 打手扩展资料")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Void> create(@Valid @RequestBody AdminCreateCompanionRequest request) {
        companionApplicationService.createCompanion(request);
        return APIResponse.ok();
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "删除陪玩", description = "只删打手扩展资料，不删登录账号；账号删除见用户管理")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Void> delete(@Parameter(description = "用户ID") @PathVariable Long userId) {
        companionApplicationService.deleteCompanion(userId);
        return APIResponse.ok();
    }

    @GetMapping
    @Operation(summary = "分页查询打手列表",
            description = "按注册时间倒序，支持按接单状态/注册时间范围/关键字（昵称/手机号/身份证号）过滤")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<PageResult<CompanionView>> list(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "接单状态") @RequestParam(required = false) CompanionStatus status,
            @Parameter(description = "注册时间范围-起（ISO-8601）")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdFrom,
            @Parameter(description = "注册时间范围-止（ISO-8601）")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdTo,
            @Parameter(description = "关键字，匹配昵称/手机号/身份证号") @RequestParam(required = false) String keyword) {
        return APIResponse.ok(companionApplicationService.listCompanions(
                pageNum, pageSize, status, keyword, createdFrom, createdTo));
    }

    @GetMapping("/stats")
    @Operation(summary = "打手统计卡片", description = "总陪玩数/可用/忙碌/平均时薪，占位实现，不受列表搜索条件影响")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<CompanionStatsView> stats() {
        return APIResponse.ok(companionApplicationService.getStats());
    }
}
