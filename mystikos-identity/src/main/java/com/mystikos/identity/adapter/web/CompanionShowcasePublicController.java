package com.mystikos.identity.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.identity.application.service.CompanionShowcaseApplicationService;
import com.mystikos.identity.application.service.CompanionShowcasePublicView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 老板浏览陪玩名片：只返回审核通过并已发布的内容，草稿/待审内容不会被看到，
 * 见 {@link com.mystikos.identity.domain.model.CompanionShowcase} 类注释。
 * 名片本身的编辑见 {@link CompanionShowcaseController}，后台审核见 {@link CompanionShowcaseAdminController}。
 */
@RestController
@RequestMapping("/api/v1/companions")
@Tag(name = "陪玩名片-老板浏览", description = "老板查看陪玩已发布的名片")
@SecurityRequirement(name = "bearerAuth")
public class CompanionShowcasePublicController {

    private final CompanionShowcaseApplicationService companionShowcaseApplicationService;

    public CompanionShowcasePublicController(CompanionShowcaseApplicationService companionShowcaseApplicationService) {
        this.companionShowcaseApplicationService = companionShowcaseApplicationService;
    }

    @GetMapping("/{userId}/showcase")
    @Operation(summary = "查看陪玩名片", description = "陪玩尚未发布过名片时返回错误")
    public APIResponse<CompanionShowcasePublicView> showcase(
            @Parameter(description = "陪玩用户ID") @PathVariable Long userId) {
        return APIResponse.ok(companionShowcaseApplicationService.getPublished(userId));
    }
}
