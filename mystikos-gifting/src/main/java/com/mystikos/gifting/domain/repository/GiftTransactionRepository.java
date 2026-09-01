package com.mystikos.gifting.domain.repository;

import com.mystikos.gifting.domain.model.GiftTransaction;

import java.math.BigDecimal;
import java.util.Optional;

public interface GiftTransactionRepository {

    /** id 为空则新增，否则整行覆盖更新（退款场景：只有 status 字段变化）。 */
    GiftTransaction save(GiftTransaction transaction);

    Optional<GiftTransaction> findById(Long id);

    /** 某老板对某礼物的累计赠送数量（quantity 累加），供 CUMULATIVE_COUNT 解锁规则评估。 */
    long sumQuantityByPatronAndGift(Long patronId, Long giftId);

    /** 某老板的累计赠礼消费总额，供 CUMULATIVE_SPEND 解锁规则评估。 */
    BigDecimal sumAmountByPatron(Long patronId);
}
