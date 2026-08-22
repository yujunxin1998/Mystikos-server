package com.mystikos.identity.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.common.security.CurrentUserContext;
import com.mystikos.identity.adapter.web.dto.AuthTokenResponse;
import com.mystikos.identity.adapter.web.dto.CurrentUserResponse;
import com.mystikos.identity.adapter.web.dto.LoginRequest;
import com.mystikos.identity.adapter.web.dto.OAuthLoginRequest;
import com.mystikos.identity.adapter.web.dto.RefreshTokenRequest;
import com.mystikos.identity.adapter.web.dto.RegisterRequest;
import com.mystikos.identity.adapter.web.dto.SendVerificationCodeRequest;
import com.mystikos.identity.application.command.LoginCommand;
import com.mystikos.identity.application.command.RegisterCommand;
import com.mystikos.identity.application.service.AuthApplicationService;
import com.mystikos.identity.application.service.AuthResult;
import com.mystikos.identity.application.service.UserApplicationService;
import com.mystikos.identity.application.service.UserProfileView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * S1 账号与认证。手机号/邮箱二选一注册登录；第三方登录接口契约已占位，
 * 调用会返回"暂未开放"（见 AuthApplicationService.loginWithOAuth 的说明）。
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "账号与认证", description = "S1：验证码、注册、登录、第三方登录、Token 刷新与登出")
public class AuthController {

    private final AuthApplicationService authApplicationService;
    private final UserApplicationService userApplicationService;

    public AuthController(AuthApplicationService authApplicationService,
                           UserApplicationService userApplicationService) {
        this.authApplicationService = authApplicationService;
        this.userApplicationService = userApplicationService;
    }

    @PostMapping("/verification-codes")
    @Operation(summary = "发送验证码", description = "本地开发环境验证码只打日志，不会真实发送短信/邮件")
    public APIResponse<Void> sendVerificationCode(@Valid @RequestBody SendVerificationCodeRequest request) {
        authApplicationService.sendVerificationCode(request.getChannel(), request.getIdentifier(),
                request.getPurpose());
        return APIResponse.ok();
    }

    @PostMapping("/register")
    @Operation(summary = "注册", description = "手机号或邮箱二选一，需先通过 /verification-codes 获取验证码")
    public APIResponse<AuthTokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResult result = authApplicationService.register(new RegisterCommand(
                request.getChannel(), request.getIdentifier(), request.getVerificationCode(),
                request.getPassword(), request.getInitialRole()));
        return APIResponse.ok(AuthTokenResponse.from(result));
    }

    @PostMapping("/login")
    @Operation(summary = "登录", description = "密码或验证码二选一")
    public APIResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = authApplicationService.login(new LoginCommand(
                request.getChannel(), request.getIdentifier(), request.getCredentialType(),
                request.getCredential()));
        return APIResponse.ok(AuthTokenResponse.from(result));
    }

    @PostMapping("/oauth/{provider}/login")
    @Operation(summary = "第三方登录", description = "已接入 Discord（provider=discord）；其他 Provider（微信……）接入方式还没定，调用会返回未开放错误")
    public APIResponse<AuthTokenResponse> loginWithOAuth(@PathVariable String provider,
                                                          @Valid @RequestBody OAuthLoginRequest request) {
        AuthResult result = authApplicationService.loginWithOAuth(provider, request.getCode());
        return APIResponse.ok(AuthTokenResponse.from(result));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "刷新 Access Token")
    public APIResponse<AuthTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResult result = authApplicationService.refresh(request.getRefreshToken());
        return APIResponse.ok(AuthTokenResponse.from(result));
    }

    @PostMapping("/logout")
    @Operation(summary = "登出", description = "吊销当前用户名下全部 Refresh Token")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Void> logout() {
        Long userId = Long.valueOf(CurrentUserContext.get().userId());
        authApplicationService.logout(userId);
        return APIResponse.ok();
    }

    @GetMapping("/me")
    @Operation(summary = "当前登录用户基础信息与角色", description = "完整资料（昵称/隐私设置）见 GET /api/v1/profile/me")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<CurrentUserResponse> me() {
        Long userId = Long.valueOf(CurrentUserContext.get().userId());
        UserProfileView profile = userApplicationService.getProfile(userId);
        return APIResponse.ok(CurrentUserResponse.from(profile));
    }
}
