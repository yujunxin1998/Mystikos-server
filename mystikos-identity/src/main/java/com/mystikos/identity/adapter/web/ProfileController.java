package com.mystikos.identity.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.common.security.CurrentUserContext;
import com.mystikos.identity.adapter.web.dto.UpdatePrivacyRequest;
import com.mystikos.identity.adapter.web.dto.UpdateProfileRequest;
import com.mystikos.identity.adapter.web.dto.UpdateUserTagsRequest;
import com.mystikos.identity.application.service.UserApplicationService;
import com.mystikos.identity.application.service.UserProfileView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * S2 用户资料——老板侧。陪玩侧资料（标签/时薪/擅长游戏/认证审核）属于
 * mystikos-provider-catalog，尚未建设，见 docs/architecture/prd-alignment.md。
 */
@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "用户资料", description = "S2（老板侧）：昵称/性别/头像/生日/个性签名/地区与隐私设置")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final UserApplicationService userApplicationService;

    public ProfileController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户完整资料")
    public APIResponse<UserProfileView> me() {
        Long userId = Long.valueOf(CurrentUserContext.get().userId());
        return APIResponse.ok(userApplicationService.getProfile(userId));
    }

    @PutMapping("/me")
    @Operation(summary = "更新资料", description = "昵称/性别/头像/生日/个性签名/地区，整体覆盖式更新")
    public APIResponse<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = Long.valueOf(CurrentUserContext.get().userId());
        userApplicationService.updateProfile(userId, request.getNickname(), request.getGender(),
                request.getAvatarObjectKey(), request.getBirthDate(), request.getBio(), request.getRegionCode());
        return APIResponse.ok();
    }

    @PutMapping("/me/tags")
    @Operation(summary = "更新我的标签", description = "游戏类型等，多选，整体覆盖式更新；标签目录见 GET /api/v1/tags")
    public APIResponse<Void> updateTags(@RequestBody UpdateUserTagsRequest request) {
        Long userId = Long.valueOf(CurrentUserContext.get().userId());
        Set<Long> tagIds = request.getTagIds() == null ? Set.of() : request.getTagIds();
        userApplicationService.updateTags(userId, tagIds);
        return APIResponse.ok();
    }

    @PutMapping("/privacy")
    @Operation(summary = "更新隐私设置", description = "是否匿名上榜")
    public APIResponse<Void> updatePrivacy(@Valid @RequestBody UpdatePrivacyRequest request) {
        Long userId = Long.valueOf(CurrentUserContext.get().userId());
        userApplicationService.updatePrivacy(userId, request.isAnonymous());
        return APIResponse.ok();
    }
}
