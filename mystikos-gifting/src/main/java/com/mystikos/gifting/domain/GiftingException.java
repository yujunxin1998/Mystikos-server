package com.mystikos.gifting.domain;

import com.mystikos.common.web.exception.BusinessException;

public class GiftingException extends BusinessException {

    public GiftingException(GiftingResponseCode code) {
        super(code);
    }

    public GiftingException(GiftingResponseCode code, String message) {
        super(code, message);
    }

    public static GiftingException notFound(Long giftId) {
        return new GiftingException(GiftingResponseCode.GIFT_NOT_FOUND, "礼物不存在或已下架：" + giftId);
    }

    public static GiftingException unlockRuleNotSatisfied() {
        return new GiftingException(GiftingResponseCode.GIFT_UNLOCK_RULE_NOT_SATISFIED);
    }

    public static GiftingException unlockRuleUnsupported() {
        return new GiftingException(GiftingResponseCode.GIFT_UNLOCK_RULE_UNSUPPORTED);
    }

    public static GiftingException insufficientBalance() {
        return new GiftingException(GiftingResponseCode.INSUFFICIENT_BALANCE);
    }

    public static GiftingException tierNotFound(Long tierId) {
        return new GiftingException(GiftingResponseCode.GIFT_TIER_NOT_FOUND, "礼物档位不存在：" + tierId);
    }

    public static GiftingException transactionNotFound(Long transactionId) {
        return new GiftingException(GiftingResponseCode.GIFT_TRANSACTION_NOT_FOUND, "赠礼流水不存在：" + transactionId);
    }

    public static GiftingException alreadyRefunded(Long transactionId) {
        return new GiftingException(GiftingResponseCode.GIFT_TRANSACTION_ALREADY_REFUNDED, "赠礼流水已退款：" + transactionId);
    }
}
