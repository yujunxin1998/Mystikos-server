package com.mystikos.relationship.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.relationship.adapter.web.dto.IntimacyLevelView;
import com.mystikos.relationship.adapter.web.dto.RelationshipSettingsResponse;
import com.mystikos.relationship.adapter.web.dto.SaveIntimacyLevelRequest;
import com.mystikos.relationship.adapter.web.dto.UpdateRelationshipSettingsRequest;
import com.mystikos.relationship.application.command.SaveIntimacyLevelCommand;
import com.mystikos.relationship.application.service.RelationshipApplicationService;
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
 * 后台管理接口：亲密度等级梯度、每日上限的维护。新增/调整等级门槛或权益文案、
 * 调整每日上限，都只是这几个接口的一次调用，不需要发版。
 */
@RestController
@RequestMapping("/api/v1/manage/relationship")
@Tag(name = "后台管理 - 亲密度", description = "亲密度等级梯度、每日上限维护")
public class RelationshipAdminController {

    private final RelationshipApplicationService relationshipApplicationService;

    public RelationshipAdminController(RelationshipApplicationService relationshipApplicationService) {
        this.relationshipApplicationService = relationshipApplicationService;
    }

    @GetMapping("/levels")
    @Operation(summary = "查询亲密度等级梯度（后台）")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<List<IntimacyLevelView>> listLevels() {
        return APIResponse.ok(relationshipApplicationService.listLevels().stream()
                .map(IntimacyLevelView::from)
                .toList());
    }

    @PostMapping("/levels")
    @Operation(summary = "新增亲密度等级")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Long> createLevel(@Valid @RequestBody SaveIntimacyLevelRequest request) {
        return APIResponse.ok(relationshipApplicationService.saveLevel(toCommand(null, request)));
    }

    @PutMapping("/levels/{levelId}")
    @Operation(summary = "编辑亲密度等级", description = "整行覆盖式更新；调整门槛不改写已有记录的历史等级变化事件")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Long> updateLevel(@Parameter(description = "等级ID") @PathVariable Long levelId,
                                          @Valid @RequestBody SaveIntimacyLevelRequest request) {
        return APIResponse.ok(relationshipApplicationService.saveLevel(toCommand(levelId, request)));
    }

    @GetMapping("/settings")
    @Operation(summary = "查询亲密度模块配置")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<RelationshipSettingsResponse> getSettings() {
        return APIResponse.ok(RelationshipSettingsResponse.from(relationshipApplicationService.getSettings()));
    }

    @PutMapping("/settings")
    @Operation(summary = "更新每日亲密度获取上限")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Void> updateSettings(@Valid @RequestBody UpdateRelationshipSettingsRequest request) {
        relationshipApplicationService.updateSettings(request.getDailyIntimacyCap());
        return APIResponse.ok(null);
    }

    private SaveIntimacyLevelCommand toCommand(Long id, SaveIntimacyLevelRequest request) {
        return new SaveIntimacyLevelCommand(id, request.getCode(), request.getDisplayNameZh(),
                request.getDisplayNameEn(), request.getThreshold(), request.getPerkDescription(),
                request.getSortOrder());
    }
}
