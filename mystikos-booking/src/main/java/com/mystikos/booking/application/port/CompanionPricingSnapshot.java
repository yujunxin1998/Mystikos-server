package com.mystikos.booking.application.port;

import java.math.BigDecimal;

/**
 * @param hourlyRate 陪玩当前小时费率
 * @param bookable   陪玩身份是否有效（未被收回）；找不到该陪玩台账时同样是 false
 */
public record CompanionPricingSnapshot(BigDecimal hourlyRate, boolean bookable) {
}
