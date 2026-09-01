package com.mystikos.gifting.domain.model;

public enum GiftTransactionStatus {
    /** 正常完成，钱已经从老板钱包转到陪玩钱包。 */
    COMPLETED,
    /** 管理端发起退款后：钱已退回老板钱包，亲密度/VIP 累计值同步扣减。 */
    REFUNDED
}
