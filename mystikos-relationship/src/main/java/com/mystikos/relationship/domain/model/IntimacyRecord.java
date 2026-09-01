package com.mystikos.relationship.domain.model;

import com.mystikos.common.level.LevelResolver;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 亲密度记录聚合根，patronId+companionId 是天然的复合业务键（落库时额外加一个
 * 自增代理主键 + 唯一约束，MyBatis-Plus 对复合主键支持不友好，见仓储实现）。
 *
 * <p>等级用字符串 code（不是裸整数序号）——十级亲密度阶梯是配置表驱动的，插入一档新等级
 * 只是表里多一行，裸整数序号会导致后面所有等级错位，字符串 code 不受影响。等级本身
 * 不在这个聚合里定义（见 {@link IntimacyLevelDefinition}），累加/回滚进度时由调用方
 * （RelationshipApplicationService）传入当前有效的等级梯度，这里只做纯计算，不做 IO。
 */
public class IntimacyRecord {

    /** 尚无任何等级配置命中兜底档时的兜底 code——正常情况下配置表里应该总有一档 threshold=0。 */
    public static final String INITIAL_LEVEL_CODE = "UNRANKED";

    private Long id;
    private final Long patronId;
    private final Long companionId;
    private String levelCode;
    private BigDecimal progressValue;
    private OffsetDateTime lastInteractionAt;

    private IntimacyRecord(Long id, Long patronId, Long companionId, String levelCode,
                            BigDecimal progressValue, OffsetDateTime lastInteractionAt) {
        this.id = id;
        this.patronId = patronId;
        this.companionId = companionId;
        this.levelCode = levelCode;
        this.progressValue = progressValue;
        this.lastInteractionAt = lastInteractionAt;
    }

    /** 首次互动时创建一条新记录，初始进度 0，等级留空到第一次 accrueProgress 才会被解析。 */
    public static IntimacyRecord initiate(Long patronId, Long companionId) {
        return new IntimacyRecord(null, patronId, companionId, INITIAL_LEVEL_CODE, BigDecimal.ZERO, OffsetDateTime.now());
    }

    /** 从持久化数据重建聚合，仅供仓储实现调用。 */
    public static IntimacyRecord restore(Long id, Long patronId, Long companionId, String levelCode,
                                          BigDecimal progressValue, OffsetDateTime lastInteractionAt) {
        return new IntimacyRecord(id, patronId, companionId, levelCode, progressValue, lastInteractionAt);
    }

    /**
     * 累加互动进度（调用方已经按每日上限裁剪过）并按当前等级梯度重算等级。
     * @return 等级是否发生变化（供调用方决定要不要发 IntimacyLevelChangedEvent）
     */
    public boolean accrueProgress(BigDecimal amount, List<IntimacyLevelDefinition> levelsAscending) {
        String previousCode = this.levelCode;
        this.progressValue = this.progressValue.add(amount);
        this.lastInteractionAt = OffsetDateTime.now();
        this.levelCode = LevelResolver.resolve(levelsAscending, this.progressValue).getCode();
        return !this.levelCode.equals(previousCode);
    }

    /**
     * 退款场景：扣减进度值（floor 在 0），允许降级——和 accrueProgress 的单调性要求不同，
     * 这是对现有"等级只增不减"假设的一次刻意松动，只在退款这一条路径上生效。
     */
    public boolean reverseProgress(BigDecimal amount, List<IntimacyLevelDefinition> levelsAscending) {
        String previousCode = this.levelCode;
        this.progressValue = this.progressValue.subtract(amount).max(BigDecimal.ZERO);
        this.levelCode = LevelResolver.resolve(levelsAscending, this.progressValue).getCode();
        return !this.levelCode.equals(previousCode);
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

    public String getLevelCode() {
        return levelCode;
    }

    public BigDecimal getProgressValue() {
        return progressValue;
    }

    public OffsetDateTime getLastInteractionAt() {
        return lastInteractionAt;
    }
}
