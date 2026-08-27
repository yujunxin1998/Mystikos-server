package com.mystikos.app.orders;

/** “我的订单”聚合视图里区分订单来源上下文的标记，不是持久化枚举，不接字典接口。 */
public enum MyOrderType {
    BOOKING,
    COMMERCE
}
