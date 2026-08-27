package com.mystikos.payment.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.common.result.ResponseCode;
import com.mystikos.common.security.CurrentUserContext;
import com.mystikos.common.web.exception.BusinessException;
import com.mystikos.payment.adapter.web.dto.PaymentCheckoutResponse;
import com.mystikos.payment.adapter.web.dto.RechargeRequest;
import com.mystikos.payment.adapter.web.dto.RejectWithdrawRequest;
import com.mystikos.payment.adapter.web.dto.WalletResponse;
import com.mystikos.payment.adapter.web.dto.WithdrawRequestRequest;
import com.mystikos.payment.adapter.web.dto.WithdrawRequestResponse;
import com.mystikos.payment.application.service.WalletApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 钱包余额、充值、陪玩提现。审批/驳回接口要求当前用户在 JWT 里带 ADMIN 角色
 * ——权限判断直接用 JWT 自带的角色声明（{@link CurrentUserContext}），不跨模块查
 * Identity，见 docs/architecture/module-structure.md 的跨模块通信规则。
 */
@RestController
@RequestMapping("/api/v1/wallet")
@Tag(name = "钱包", description = "内部记账余额：充值/礼物扣款走这里的余额，提现走 Stripe Connect")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletApplicationService walletApplicationService;

    public WalletController(WalletApplicationService walletApplicationService) {
        this.walletApplicationService = walletApplicationService;
    }

    @GetMapping
    @Operation(summary = "查询余额", description = "currency 只在钱包第一次开通时生效，之后传别的币种会报错")
    public APIResponse<WalletResponse> getBalance(@RequestParam String currency) {
        return APIResponse.ok(WalletResponse.from(walletApplicationService.getBalance(currentUserId(), currency)));
    }

    @PostMapping("/recharge")
    @Operation(summary = "发起充值", description = "返回前端完成支付所需的 payload，具体形状见 payloadType")
    public APIResponse<PaymentCheckoutResponse> recharge(@Valid @RequestBody RechargeRequest request) {
        var result = walletApplicationService.requestRecharge(currentUserId(), request.getAmount(), request.getCurrency(),
                request.getProvider(), request.getScene());
        return APIResponse.ok(PaymentCheckoutResponse.from(result));
    }

    @GetMapping("/connect/onboarding-link")
    @Operation(summary = "陪玩发起 Stripe Connect 入驻", description = "返回一次性 onboarding 跳转链接，完成资料后才能被批准提现")
    public APIResponse<String> connectOnboardingLink(@RequestParam String email,
                                                       @RequestParam String returnUrl,
                                                       @RequestParam String refreshUrl) {
        return APIResponse.ok(walletApplicationService.startConnectOnboarding(currentUserId(), email, returnUrl, refreshUrl));
    }

    @PostMapping("/withdraw-requests")
    @Operation(summary = "申请提现", description = "申请即刻冻结对应余额，审核通过才真正打款")
    public APIResponse<WithdrawRequestResponse> requestWithdraw(@Valid @RequestBody WithdrawRequestRequest request) {
        var withdraw = walletApplicationService.requestWithdraw(currentUserId(), request.getAmount(), request.getCurrency());
        return APIResponse.ok(WithdrawRequestResponse.from(withdraw));
    }

    @GetMapping("/withdraw-requests")
    @Operation(summary = "我的提现申请列表")
    public APIResponse<List<WithdrawRequestResponse>> listWithdrawRequests() {
        return APIResponse.ok(walletApplicationService.listWithdrawRequests(currentUserId()).stream()
                .map(WithdrawRequestResponse::from)
                .toList());
    }

    @PostMapping("/withdraw-requests/{id}/approve")
    @Operation(summary = "审批通过并打款", description = "仅 ADMIN；审批通过后立即调用 Stripe Connect 打款")
    public APIResponse<WithdrawRequestResponse> approveWithdraw(@Parameter(description = "提现申请ID") @PathVariable Long id) {
        requireAdmin();
        return APIResponse.ok(WithdrawRequestResponse.from(walletApplicationService.approveWithdraw(id, currentUserId())));
    }

    @PostMapping("/withdraw-requests/{id}/reject")
    @Operation(summary = "驳回提现申请", description = "仅 ADMIN；驳回后把冻结的余额退回给陪玩")
    public APIResponse<WithdrawRequestResponse> rejectWithdraw(@Parameter(description = "提现申请ID") @PathVariable Long id,
                                                                @Valid @RequestBody RejectWithdrawRequest request) {
        requireAdmin();
        return APIResponse.ok(WithdrawRequestResponse.from(
                walletApplicationService.rejectWithdraw(id, currentUserId(), request.getReason())));
    }

    private void requireAdmin() {
        if (!CurrentUserContext.get().hasRole("ADMIN")) {
            throw new BusinessException(ResponseCode.FORBIDDEN);
        }
    }

    private Long currentUserId() {
        return Long.valueOf(CurrentUserContext.get().userId());
    }
}
