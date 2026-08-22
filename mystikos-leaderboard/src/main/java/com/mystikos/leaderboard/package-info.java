/**
 * 排行榜与统计（Leaderboard &amp; Stats）限界上下文：纯读侧，无写聚合。
 * 订阅 BookingCompleted / GiftSent 事件累加数值，定时任务周期重算排名快照
 * （CQRS Projection）。领域模型见 docs/architecture/domain-model.md；
 * 内部结构实现时参照 mystikos-booking 模块（但没有写聚合，只有事件消费者
 * 和定时任务），见 docs/architecture/module-structure.md。
 */
package com.mystikos.leaderboard;
