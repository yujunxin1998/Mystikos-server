package com.mystikos.membership.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.membership.adapter.web.dto.MembershipTierView;
import com.mystikos.membership.adapter.web.dto.SaveMembershipTierRequest;
import com.mystikos.membership.application.command.SaveMembershipTierCommand;
import com.mystikos.membership.application.service.MembershipApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台管理接口：VIP 等级梯度的维护。新增/调整等级门槛或权益文案只是这两个接口的
 * 一次调用，不需要发版。
 */
@RestController
@RequestMapping("/api/v1/manage/membership")
@Tag(name = "后台管理 - 会员成长", description = "VIP 等级梯度维护")
public class MembershipAdminController {

    private final MembershipApplicationService membershipApplicationService;

    public MembershipAdminController(MembershipApplicationService membershipApplicationService) {
        this.membershipApplicationService = membershipApplicationService;
    }

    @GetMapping("/tiers")
    @Operation(summary = "查询 VIP 等级梯度（后台）")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<List<MembershipTierView>> listTiers() {
        return APIResponse.ok(membershipApplicationService.listTiers().stream()
                .map(MembershipTierView::from)
                .toList());
    }

    @PostMapping("/tiers")
    @Operation(summary = "新增 VIP 等级")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Long> createTier(@Valid @RequestBody SaveMembershipTierRequest request) {
        return APIResponse.ok(membershipApplicationService.saveTier(toCommand(null, request)));
    }

    @PutMapping("/tiers/{tierId}")
    @Operation(summary = "编辑 VIP 等级", description = "整行覆盖式更新；调整门槛不改写已有账户的历史升级事件")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Long> updateTier(@Parameter(description = "等级ID") @PathVariable Long tierId,
                                         @Valid @RequestBody SaveMembershipTierRequest request) {
        return APIResponse.ok(membershipApplicationService.saveTier(toCommand(tierId, request)));
    }

    private SaveMembershipTierCommand toCommand(Long id, SaveMembershipTierRequest request) {
        return new SaveMembershipTierCommand(id, request.getCode(), request.getDisplayName(),
                request.getDisplayNameEn(), request.getLevel(), request.getCumulativeSpendThreshold(),
                request.getPerkDescription(), request.getSortOrder());
    }
}
