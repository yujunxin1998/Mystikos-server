package com.mystikos.identity.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.common.result.PageResult;
import com.mystikos.common.security.CurrentUserContext;
import com.mystikos.identity.adapter.web.dto.ReviewCompanionShowcaseRequest;
import com.mystikos.identity.application.service.CompanionShowcaseApplicationService;
import com.mystikos.identity.application.service.CompanionShowcaseView;
import com.mystikos.identity.domain.model.CompanionShowcaseRevisionStatus;
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
 * 后台管理接口：陪玩名片审核队列检索、录入审核结果。归入 {@code /api/v1/manage/**} 路由前缀，
 * 见 {@link UserController} 类注释。审核人取当前登录管理员，不像陪玩身份申请那样区分线下
 * 考核人和录入人——名片审核就是内容审核，谁点的通过/驳回就是谁。
 */
@RestController
@RequestMapping("/api/v1/manage/companion-showcases")
@Tag(name = "后台管理 - 陪玩名片审核", description = "名片审核队列检索/录入审核结果")
public class CompanionShowcaseAdminController {

    private final CompanionShowcaseApplicationService companionShowcaseApplicationService;

    public CompanionShowcaseAdminController(CompanionShowcaseApplicationService companionShowcaseApplicationService) {
        this.companionShowcaseApplicationService = companionShowcaseApplicationService;
    }

    @GetMapping
    @Operation(summary = "分页查询陪玩名片列表",
            description = "按提交时间倒序，支持按状态/提交时间范围/关键字（陪玩昵称/手机号/邮箱）过滤")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<PageResult<CompanionShowcaseView>> list(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "名片状态") @RequestParam(required = false) CompanionShowcaseRevisionStatus status,
            @Parameter(description = "提交时间范围-起（ISO-8601）")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdFrom,
            @Parameter(description = "提交时间范围-止（ISO-8601）")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime createdTo,
            @Parameter(description = "关键字，匹配陪玩昵称/手机号/邮箱") @RequestParam(required = false) String keyword) {
        return APIResponse.ok(companionShowcaseApplicationService.list(
                pageNum, pageSize, status, keyword, createdFrom, createdTo));
    }

    @PutMapping("/{revisionId}/review")
    @Operation(summary = "录入名片审核结果", description = "通过则老板端立即看到新内容；不通过必须填写驳回原因")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Void> review(@Parameter(description = "名片草稿记录ID") @PathVariable Long revisionId,
                                     @Valid @RequestBody ReviewCompanionShowcaseRequest request) {
        Long reviewerId = Long.valueOf(CurrentUserContext.get().userId());
        companionShowcaseApplicationService.review(revisionId, reviewerId, request.getApproved(), request.getComment());
        return APIResponse.ok();
    }
}
