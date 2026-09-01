package com.mystikos.gifting.domain;

import com.mystikos.common.result.IResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Gifting 上下文错误码，号段 7000-7999（见 docs/architecture/exception-handling.md）。
 */
@Getter
@AllArgsConstructor
public enum GiftingResponseCode implements IResponseCode {

    GIFT_NOT_FOUND(7001, "礼物不存在或已下架"),
    GIFT_UNLOCK_RULE_NOT_SATISFIED(7002, "还没有达到这份礼物的解锁条件"),
    GIFT_UNLOCK_RULE_UNSUPPORTED(7003, "该礼物的解锁规则暂不支持自动评估，无法赠送"),
    INSUFFICIENT_BALANCE(7004, "钱包余额不足，请先充值"),
    GIFT_TIER_NOT_FOUND(7005, "礼物档位不存在"),
    GIFT_TRANSACTION_NOT_FOUND(7006, "赠礼流水不存在"),
    GIFT_TRANSACTION_ALREADY_REFUNDED(7007, "这笔赠礼已经退过款");

    private final int code;
    private final String message;
}
