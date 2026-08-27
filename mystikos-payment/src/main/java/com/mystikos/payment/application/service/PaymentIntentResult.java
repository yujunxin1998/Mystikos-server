package com.mystikos.payment.application.service;

import com.mystikos.payment.application.port.PaymentPayloadType;
import com.mystikos.payment.domain.model.PaymentIntent;
import com.mystikos.payment.domain.model.PaymentStatus;

import java.util.Map;

/**
 * PaymentApplicationService 对外暴露的结果视图。Booking/Commerce/Gifting 各自的
 * PaymentPort 实现（防腐层）把这个类型翻译成自己模块的返回类型，不直接把它透出到
 * 自己的 application 层之外。
 */
public record PaymentIntentResult(Long intentId, PaymentPayloadType payloadType, Map<String, String> payload,
                                   PaymentStatus status) {

    public static PaymentIntentResult from(PaymentIntent intent) {
        return new PaymentIntentResult(intent.getId(), intent.getPayloadType(), intent.getPayload(), intent.getStatus());
    }
}
