package com.mystikos.gifting.domain.model;

/**
 * 礼物解锁规则类型，见 docs/architecture/domain-model.md 的 Gifting 设计。
 * 目前只有 {@link #CUMULATIVE_COUNT}/{@link #CUMULATIVE_SPEND} 在 Gifting 自己的
 * 交易流水表内就能算出来，评估逻辑已实现；{@link #CONSECUTIVE_DAYS}/{@link #LEADERBOARD_RANK}/
 * {@link #INTIMACY_STAGE} 需要跨上下文查询 Leaderboard/Relationship——但这两个上下文要订阅
 * Gifting 发布的 GiftSentEvent（见 docs），反过来加一个 Gifting 查询它们的 Port 会形成
 * 循环模块依赖，Maven 编不过。这三种规则先只允许存储配置，不做解锁判定
 * （见 GiftingException#unlockRuleUnsupported）。
 */
public enum UnlockRuleType {
    /** 无条件，随时可赠送 */
    NONE,
    /** 累计赠送数量（quantity 累加）达到阈值 */
    CUMULATIVE_COUNT,
    /** 累计消费金额达到阈值 */
    CUMULATIVE_SPEND,
    /** 连续互动天数达到阈值——评估逻辑尚未实现 */
    CONSECUTIVE_DAYS,
    /** 排行榜名次达到阈值——评估逻辑尚未实现 */
    LEADERBOARD_RANK,
    /** 亲密度阶段达到阈值——评估逻辑尚未实现 */
    INTIMACY_STAGE
}
