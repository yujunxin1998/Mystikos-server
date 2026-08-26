package com.mystikos.identity.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.common.security.CurrentUserContext;
import com.mystikos.identity.adapter.web.dto.OAuthAuthorizeUrlResponse;
import com.mystikos.identity.adapter.web.dto.OAuthBindAuthorizeRequest;
import com.mystikos.identity.adapter.web.dto.OAuthUnbindRequest;
import com.mystikos.identity.adapter.web.dto.UpdatePrivacyRequest;
import com.mystikos.identity.adapter.web.dto.UpdateProfileRequest;
import com.mystikos.identity.adapter.web.dto.UpdateUserTagsRequest;
import com.mystikos.identity.adapter.web.dto.ContactVerificationCodeRequest;
import com.mystikos.identity.adapter.web.dto.VerifyContactRequest;
import com.mystikos.identity.application.service.AuthApplicationService;
import com.mystikos.identity.application.service.CompanionApplicationReadinessView;
import com.mystikos.identity.application.service.OAuthFlowService;
import com.mystikos.identity.application.service.UserApplicationService;
import com.mystikos.identity.application.service.UserProfileView;
import io.swagger.v3.oas.annotations.Operation;
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

import java.net.URI;
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
    private final AuthApplicationService authApplicationService;
    private final OAuthFlowService oauthFlowService;

    public ProfileController(UserApplicationService userApplicationService,
                             AuthApplicationService authApplicationService,
                             OAuthFlowService oauthFlowService) {
        this.userApplicationService = userApplicationService;
        this.authApplicationService = authApplicationService;
        this.oauthFlowService = oauthFlowService;
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户完整资料")
    public APIResponse<UserProfileView> me() {
        Long userId = Long.valueOf(CurrentUserContext.get().userId());
        return APIResponse.ok(userApplicationService.getProfile(userId));
    }

    @GetMapping("/me/companion-readiness")
    @Operation(summary = "获取陪玩申请联系方式资格", description = "邮箱或手机号任意认证一项即可申请")
    public APIResponse<CompanionApplicationReadinessView> companionReadiness() {
        Long userId = Long.valueOf(CurrentUserContext.get().userId());
        return APIResponse.ok(userApplicationService.getCompanionApplicationReadiness(userId));
    }

    @PostMapping("/me/contact-verification-codes")
    @Operation(summary = "发送联系方式绑定验证码")
    public APIResponse<Void> sendContactVerificationCode(
            @Valid @RequestBody ContactVerificationCodeRequest request) {
        Long userId = Long.valueOf(CurrentUserContext.get().userId());
        authApplicationService.sendContactVerificationCode(userId, request.getChannel(), request.getIdentifier());
        return APIResponse.ok();
    }

    @PutMapping("/me/contacts")
    @Operation(summary = "验证并绑定邮箱或手机号")
    public APIResponse<CompanionApplicationReadinessView> bindContact(
            @Valid @RequestBody VerifyContactRequest request) {
        Long userId = Long.valueOf(CurrentUserContext.get().userId());
        authApplicationService.bindVerifiedContact(userId, request.getChannel(), request.getIdentifier(),
                request.getVerificationCode());
        return APIResponse.ok(userApplicationService.getCompanionApplicationReadiness(userId));
    }

    @PostMapping("/me/oauth/{provider}/binding-verification-codes")
    @Operation(summary = "发送第三方账号绑定/换绑/解绑验证码",
            description = "验证码发到当前账号已绑定的邮箱或手机号，不接受调用方指定收件地址；"
                    + "绑定/换绑/解绑第三方账号前都必须先拿这个验证码做二次确认")
    public APIResponse<Void> sendOAuthBindingVerificationCode(@PathVariable String provider) {
        Long userId = Long.valueOf(CurrentUserContext.get().userId());
        authApplicationService.sendOAuthBindingVerificationCode(userId);
        return APIResponse.ok();
    }

    @PostMapping("/me/oauth/{provider}/bind-authorize")
    @Operation(summary = "发起第三方账号绑定/换绑",
            description = "校验二次确认验证码后返回 Discord 授权 URL，由前端自行跳转；"
                    + "同一 provider 已有绑定时再次调用视为换绑，旧绑定会被替换；"
                    + "授权结果通过 /api/v1/auth/oauth/bind-tickets 兑换")
    public APIResponse<OAuthAuthorizeUrlResponse> bindOAuthAuthorize(@PathVariable String provider,
            @Valid @RequestBody OAuthBindAuthorizeRequest request) {
        Long userId = Long.valueOf(CurrentUserContext.get().userId());
        URI authorizeUri = oauthFlowService.beginBindAuthorization(provider, userId, request.getVerificationCode());
        return APIResponse.ok(new OAuthAuthorizeUrlResponse(authorizeUri.toString()));
    }

    @PostMapping("/me/oauth/{provider}/unbind")
    @Operation(summary = "解绑第三方账号",
            description = "需先校验二次确认验证码；解绑后账号必须仍保留手机号/邮箱/其他第三方绑定之一，否则拒绝。"
                    + "用 POST 而不是 DELETE + body，避免部分网关/客户端丢弃 DELETE 请求体")
    public APIResponse<Void> unbindOAuth(@PathVariable String provider,
            @Valid @RequestBody OAuthUnbindRequest request) {
        Long userId = Long.valueOf(CurrentUserContext.get().userId());
        authApplicationService.unbindOAuthProvider(userId, provider, request.getVerificationCode());
        return APIResponse.ok();
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
