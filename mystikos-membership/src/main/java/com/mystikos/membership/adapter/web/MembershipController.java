package com.mystikos.membership.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.membership.adapter.web.dto.MembershipResponse;
import com.mystikos.membership.application.service.MembershipApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 只读查询——等级推进由事件驱动（见 infrastructure/acl/GiftSentEventListener，
 * 临时顶替方案，Payment 落地后要换），不提供直接写接口。
 */
@RestController
@RequestMapping("/api/v1/memberships")
@Tag(name = "会员成长", description = "老板会员等级查询")
public class MembershipController {

    private final MembershipApplicationService membershipApplicationService;

    public MembershipController(MembershipApplicationService membershipApplicationService) {
        this.membershipApplicationService = membershipApplicationService;
    }

    @GetMapping("/{patronId}")
    @Operation(summary = "查询会员等级", description = "尚无消费记录时返回 LV1、累计消费 0，不报 404")
    public APIResponse<MembershipResponse> get(@Parameter(description = "老板用户ID") @PathVariable Long patronId) {
        return APIResponse.ok(MembershipResponse.from(membershipApplicationService.getMembership(patronId)));
    }
}
