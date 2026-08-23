package com.mystikos.identity.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.identity.adapter.web.dto.CreateTagRequest;
import com.mystikos.identity.adapter.web.dto.TagResponse;
import com.mystikos.identity.application.service.TagApplicationService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标签目录（游戏类型等），后台配置、前台多选。查询公开、写操作运营态——
 * 权限校验粒度同 {@link UserController}（仅要求登录，不做角色级 @PreAuthorize），
 * 是这个代码库当前一致的做法，不是本次新引入的松口子。
 */
@RestController
@RequestMapping("/api/v1/tags")
@Tag(name = "标签目录", description = "游戏类型等标签的后台配置与查询")
public class TagController {

    private final TagApplicationService tagApplicationService;

    public TagController(TagApplicationService tagApplicationService) {
        this.tagApplicationService = tagApplicationService;
    }

    @GetMapping
    @Operation(summary = "查询标签列表", description = "默认只返回启用中的标签，供前台多选控件使用")
    public APIResponse<List<TagResponse>> list(
            @Parameter(description = "分类，如 GAME_TYPE") @RequestParam String category,
            @Parameter(description = "是否只返回启用中的标签，默认true") @RequestParam(defaultValue = "true") boolean onlyEnabled) {
        return APIResponse.ok(tagApplicationService.listTags(category, onlyEnabled).stream()
                .map(TagResponse::from)
                .toList());
    }

    @PostMapping
    @Operation(summary = "新建标签", description = "运营态操作，默认启用")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Long> create(@Valid @RequestBody CreateTagRequest request) {
        return APIResponse.ok(tagApplicationService.createTag(request.getCategory(), request.getLabel(),
                request.getSortOrder()));
    }

    @PutMapping("/{tagId}/enable")
    @Operation(summary = "启用标签")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Void> enable(@Parameter(description = "标签ID") @PathVariable Long tagId) {
        tagApplicationService.enableTag(tagId);
        return APIResponse.ok();
    }

    @PutMapping("/{tagId}/disable")
    @Operation(summary = "停用标签", description = "停用后不再出现在前台多选列表，但不影响已经选过的用户")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Void> disable(@Parameter(description = "标签ID") @PathVariable Long tagId) {
        tagApplicationService.disableTag(tagId);
        return APIResponse.ok();
    }
}
