package com.mystikos.payment.domain;

import com.mystikos.common.web.exception.BusinessException;

public class PaymentException extends BusinessException {

    public PaymentException(PaymentResponseCode code) {
        super(code);
    }

    public PaymentException(PaymentResponseCode code, String message) {
        super(code, message);
    }

    public static PaymentException notFound(Long intentId) {
        return new PaymentException(PaymentResponseCode.INTENT_NOT_FOUND, "支付意图不存在：" + intentId);
    }

    public static PaymentException statusInvalid(String message) {
        return new PaymentException(PaymentResponseCode.STATUS_INVALID, message);
    }

    public static PaymentException gatewayNotConfigured() {
        return new PaymentException(PaymentResponseCode.GATEWAY_NOT_CONFIGURED);
    }

    public static PaymentException gatewayError(String message) {
        return new PaymentException(PaymentResponseCode.GATEWAY_ERROR, message);
    }

    public static PaymentException webhookSignatureInvalid() {
        return new PaymentException(PaymentResponseCode.WEBHOOK_SIGNATURE_INVALID);
    }

    public static PaymentException insufficientBalance() {
        return new PaymentException(PaymentResponseCode.INSUFFICIENT_BALANCE);
    }

    public static PaymentException withdrawRequestNotFound(Long id) {
        return new PaymentException(PaymentResponseCode.WITHDRAW_REQUEST_NOT_FOUND, "提现申请不存在：" + id);
    }

    public static PaymentException payoutAccountNotReady() {
        return new PaymentException(PaymentResponseCode.PAYOUT_ACCOUNT_NOT_READY);
    }
}
