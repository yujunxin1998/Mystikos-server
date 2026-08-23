package com.mystikos.identity.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.common.result.PageResult;
import com.mystikos.identity.adapter.web.dto.ReviewCompanionApplicationRequest;
import com.mystikos.identity.application.service.CompanionIdentityApplicationService;
import com.mystikos.identity.application.service.CompanionIdentityApplicationView;
import com.mystikos.identity.domain.model.CompanionIdentityApplicationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

/**
 * 后台管理接口：陪玩身份申请的列表检索、开始考核、录入考核结果。
 * 归入 {@code /api/v1/manage/**} 路由前缀，见 {@link UserController} 类注释。
 * 审核通过后自动开通的陪玩台账管理见 {@link CompanionController}。
 */
@RestController
@RequestMapping("/api/v1/manage/companion-applications")
@Tag(name = "后台管理 - 陪玩身份申请审核", description = "申请列表检索/开始考核/录入考核结果")
public class CompanionApplicationAdminController {

    private final CompanionIdentityApplicationService companionIdentityApplicationService;

    public CompanionApplicationAdminController(
            CompanionIdentityApplicationService companionIdentityApplicationService) {
        this.companionIdentityApplicationService = companionIdentityApplicationService;
    }

    @GetMapping
    @Operation(summary = "分页查询陪玩身份申请列表",
            description = "按提交时间倒序，支持按状态/提交时间范围/关键字（真实姓名/游戏昵称/联系手机号/联系邮箱）过滤")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<PageResult<CompanionIdentityApplicationView>> list(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "申请状态") @RequestParam(required = false) CompanionIdentityApplicationStatus status,
            @Parameter(description = "提交时间范围-起（ISO-8601）")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdFrom,
            @Parameter(description = "提交时间范围-止（ISO-8601）")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdTo,
            @Parameter(description = "关键字，匹配真实姓名/游戏昵称/联系手机号/联系邮箱") @RequestParam(required = false) String keyword) {
        return APIResponse.ok(companionIdentityApplicationService.list(
                pageNum, pageSize, status, keyword, createdFrom, createdTo));
    }

    @PutMapping("/{id}/start-assessment")
    @Operation(summary = "开始考核", description = "仅状态流转（申请中 -> 考核中），考核过程线下进行")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Void> startAssessment(@Parameter(description = "申请ID") @PathVariable Long id) {
        companionIdentityApplicationService.startAssessment(id);
        return APIResponse.ok();
    }

    @PutMapping("/{id}/review")
    @Operation(summary = "录入考核结果",
            description = "考核人、考核结果必填，审核意见可为空；通过则自动开通陪玩身份")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Void> review(@Parameter(description = "申请ID") @PathVariable Long id,
                                     @Valid @RequestBody ReviewCompanionApplicationRequest request) {
        companionIdentityApplicationService.review(id, request.getReviewerId(), request.getResult(),
                request.getComment());
        return APIResponse.ok();
    }
}
