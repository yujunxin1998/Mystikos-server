package com.mystikos.relationship.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 亲密度记录聚合根，patronId+companionId 是天然的复合业务键（落库时额外加一个
 * 自增代理主键 + 唯一约束，MyBatis-Plus 对复合主键支持不友好，见仓储实现）。
 */
public class IntimacyRecord {

    private Long id;
    private final Long patronId;
    private final Long companionId;
    private int stage;
    private BigDecimal progressValue;
    private OffsetDateTime lastInteractionAt;

    private IntimacyRecord(Long id, Long patronId, Long companionId, int stage,
                            BigDecimal progressValue, OffsetDateTime lastInteractionAt) {
        this.id = id;
        this.patronId = patronId;
        this.companionId = companionId;
        this.stage = stage;
        this.progressValue = progressValue;
        this.lastInteractionAt = lastInteractionAt;
    }

    /** 首次互动时创建一条新记录，初始阶段 0。 */
    public static IntimacyRecord initiate(Long patronId, Long companionId) {
        return new IntimacyRecord(null, patronId, companionId, 0, BigDecimal.ZERO, OffsetDateTime.now());
    }

    /** 从持久化数据重建聚合，仅供仓储实现调用。 */
    public static IntimacyRecord restore(Long id, Long patronId, Long companionId, int stage,
                                          BigDecimal progressValue, OffsetDateTime lastInteractionAt) {
        return new IntimacyRecord(id, patronId, companionId, stage, progressValue, lastInteractionAt);
    }

    /**
     * 累加互动进度并按 {@link IntimacyStagePolicy} 重算阶段。
     * @return 阶段是否发生变化（供调用方决定要不要发 IntimacyStageChangedEvent）
     */
    public boolean accrueProgress(BigDecimal amount) {
        int previousStage = this.stage;
        this.progressValue = this.progressValue.add(amount);
        this.lastInteractionAt = OffsetDateTime.now();
        this.stage = IntimacyStagePolicy.resolveStage(this.progressValue);
        return this.stage != previousStage;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getPatronId() {
        return patronId;
    }

    public Long getCompanionId() {
        return companionId;
    }

    public int getStage() {
        return stage;
    }

    public BigDecimal getProgressValue() {
        return progressValue;
    }

    public OffsetDateTime getLastInteractionAt() {
        return lastInteractionAt;
    }
}
