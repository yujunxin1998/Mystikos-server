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
}
