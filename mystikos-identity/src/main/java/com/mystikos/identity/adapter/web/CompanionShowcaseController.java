package com.mystikos.identity.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.common.security.CurrentUserContext;
import com.mystikos.identity.adapter.web.dto.SaveCompanionShowcaseDraftRequest;
import com.mystikos.identity.application.service.CompanionShowcaseApplicationService;
import com.mystikos.identity.application.service.CompanionShowcaseView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 陪玩自己维护名片：编辑草稿、提交审核、自查状态。名片对外展示（老板浏览）见
 * {@link CompanionShowcasePublicController}；后台审核操作见 {@link CompanionShowcaseAdminController}。
 */
@RestController
@RequestMapping("/api/v1/companion-showcase")
@Tag(name = "陪玩名片", description = "陪玩自己编辑名片草稿、提交审核、自查状态")
@SecurityRequirement(name = "bearerAuth")
public class CompanionShowcaseController {

    private final CompanionShowcaseApplicationService companionShowcaseApplicationService;

    public CompanionShowcaseController(CompanionShowcaseApplicationService companionShowcaseApplicationService) {
        this.companionShowcaseApplicationService = companionShowcaseApplicationService;
    }

    @GetMapping("/me")
    @Operation(summary = "查看我的名片草稿", description = "取最近一次编辑/提交的记录，含审核状态和已发布信息；没编辑过时返回空")
    public APIResponse<CompanionShowcaseView> me() {
        Long userId = Long.valueOf(CurrentUserContext.get().userId());
        return APIResponse.ok(companionShowcaseApplicationService.getMyDraft(userId));
    }

    @PutMapping("/me/draft")
    @Operation(summary = "保存名片草稿",
            description = "照片/视频/语音取 /api/v1/files/upload 返回的 objectKey；待审核状态下不能编辑，需等审核结果")
    public APIResponse<Void> saveDraft(@Valid @RequestBody SaveCompanionShowcaseDraftRequest request) {
        Long userId = Long.valueOf(CurrentUserContext.get().userId());
        companionShowcaseApplicationService.saveDraft(userId, request.getBio(), request.getTagIds(),
                request.getPhotoObjectKeys(), request.getVideoObjectKeys(), request.getAudioObjectKeys());
        return APIResponse.ok();
    }

    @PostMapping("/me/submit")
    @Operation(summary = "提交名片审核", description = "至少需要一张照片和一个游戏标签；审核通过前老板端看到的仍是上一个已发布版本")
    public APIResponse<Void> submit() {
        Long userId = Long.valueOf(CurrentUserContext.get().userId());
        companionShowcaseApplicationService.submit(userId);
        return APIResponse.ok();
    }
}
