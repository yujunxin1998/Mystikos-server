package com.mystikos.payment.domain;

import com.mystikos.common.result.IResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Payment 上下文错误码，号段 9000-9999（见 docs/architecture/exception-handling.md）。
 */
@Getter
@AllArgsConstructor
public enum PaymentResponseCode implements IResponseCode {

    INTENT_NOT_FOUND(9001, "支付意图不存在"),
    STATUS_INVALID(9002, "当前支付状态不允许该操作"),
    GATEWAY_NOT_CONFIGURED(9003, "支付网关未配置"),
    GATEWAY_ERROR(9004, "支付网关调用失败"),
    WEBHOOK_SIGNATURE_INVALID(9005, "支付回调签名校验失败"),
    INSUFFICIENT_BALANCE(9006, "钱包余额不足"),
    WITHDRAW_REQUEST_NOT_FOUND(9007, "提现申请不存在"),
    PAYOUT_ACCOUNT_NOT_READY(9008, "收款账户尚未完成入驻");

    private final int code;
    private final String message;
}
