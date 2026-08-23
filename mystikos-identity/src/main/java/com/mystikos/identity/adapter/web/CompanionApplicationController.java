package com.mystikos.identity.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.common.security.CurrentUserContext;
import com.mystikos.identity.adapter.web.dto.SubmitCompanionApplicationRequest;
import com.mystikos.identity.application.service.CompanionIdentityApplicationService;
import com.mystikos.identity.application.service.CompanionIdentityApplicationView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会员侧陪玩身份申请：提交申请、自查申请状态。后台考核操作见
 * {@link CompanionApplicationAdminController}；审核通过后自动开通的陪玩台账管理见 {@link CompanionController}。
 */
@RestController
@RequestMapping("/api/v1/companion-applications")
@Tag(name = "陪玩身份申请", description = "会员申请陪玩身份、自查申请状态")
@SecurityRequirement(name = "bearerAuth")
public class CompanionApplicationController {

    private final CompanionIdentityApplicationService companionIdentityApplicationService;

    public CompanionApplicationController(CompanionIdentityApplicationService companionIdentityApplicationService) {
        this.companionIdentityApplicationService = companionIdentityApplicationService;
    }

    @PostMapping
    @Operation(summary = "提交陪玩身份申请",
            description = "第三方登录账号需先完善邮箱和手机号；已有进行中的申请不能重复提交")
    public APIResponse<Long> submit(@Valid @RequestBody SubmitCompanionApplicationRequest request) {
        Long userId = Long.valueOf(CurrentUserContext.get().userId());
        return APIResponse.ok(companionIdentityApplicationService.submit(userId, request));
    }

    @GetMapping("/me")
    @Operation(summary = "查看我的陪玩身份申请", description = "取最近一次提交的申请，没申请过时返回空")
    public APIResponse<CompanionIdentityApplicationView> me() {
        Long userId = Long.valueOf(CurrentUserContext.get().userId());
        return APIResponse.ok(companionIdentityApplicationService.getLatestForUser(userId));
    }
}
