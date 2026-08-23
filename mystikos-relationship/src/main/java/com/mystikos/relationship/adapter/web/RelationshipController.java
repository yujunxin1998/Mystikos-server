package com.mystikos.relationship.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.relationship.adapter.web.dto.IntimacyResponse;
import com.mystikos.relationship.application.service.RelationshipApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 只读查询——亲密度的推进由订阅 GiftSent 等事件驱动（见
 * infrastructure/acl/GiftSentEventListener），不提供直接写接口。
 */
@RestController
@RequestMapping("/api/v1/relationships")
@Tag(name = "亲密度", description = "老板×陪玩的亲密度阶段查询")
public class RelationshipController {

    private final RelationshipApplicationService relationshipApplicationService;

    public RelationshipController(RelationshipApplicationService relationshipApplicationService) {
        this.relationshipApplicationService = relationshipApplicationService;
    }

    @GetMapping("/{patronId}/{companionId}")
    @Operation(summary = "查询亲密度", description = "尚无互动记录时返回阶段 0、进度 0，不报 404")
    public APIResponse<IntimacyResponse> get(
            @Parameter(description = "老板用户ID") @PathVariable Long patronId,
            @Parameter(description = "陪玩用户ID") @PathVariable Long companionId) {
        return APIResponse.ok(IntimacyResponse.from(
                relationshipApplicationService.getIntimacy(patronId, companionId)));
    }
}
