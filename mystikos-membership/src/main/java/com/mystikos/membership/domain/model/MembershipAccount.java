package com.mystikos.membership.domain.model;

import com.mystikos.common.level.LevelResolver;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 会员成长账户聚合根，与老板用户 1:1。等级用字符串 code（不是枚举实例）——VIP 梯度
 * 是配置表驱动的（见 {@link MembershipTierDefinition}），聚合本身不知道梯度长什么样，
 * 累加/回滚消费时由调用方（MembershipApplicationService）传入当前有效的梯度，
 * 这里只做纯计算，不做 IO。
 */
public class MembershipAccount {

    /** 尚未解析出真实等级时的占位 code——首次 accrueSpend 会立即替换成真实的最低档。 */
    public static final String INITIAL_TIER_CODE = "UNRANKED";

    private Long id;
    private final Long patronId;
    private String currentTierCode;
    private BigDecimal cumulativeSpend;
    private OffsetDateTime tierUpgradedAt;

    private MembershipAccount(Long id, Long patronId, String currentTierCode,
                               BigDecimal cumulativeSpend, OffsetDateTime tierUpgradedAt) {
        this.id = id;
        this.patronId = patronId;
        this.currentTierCode = currentTierCode;
        this.cumulativeSpend = cumulativeSpend;
        this.tierUpgradedAt = tierUpgradedAt;
    }

    public static MembershipAccount initiate(Long patronId) {
        return new MembershipAccount(null, patronId, INITIAL_TIER_CODE, BigDecimal.ZERO, OffsetDateTime.now());
    }

    /** 从持久化数据重建聚合，仅供仓储实现调用。 */
    public static MembershipAccount restore(Long id, Long patronId, String currentTierCode,
                                             BigDecimal cumulativeSpend, OffsetDateTime tierUpgradedAt) {
        return new MembershipAccount(id, patronId, currentTierCode, cumulativeSpend, tierUpgradedAt);
    }

    /**
     * 累加消费并按新的累计值重算等级。降级不存在——累计消费只会增长，见
     * {@link #reverseSpend} 处理退款场景的降级需求。
     * @return 是否发生升级
     */
    public boolean accrueSpend(BigDecimal amount, List<MembershipTierDefinition> tiersAscending) {
        String previousCode = this.currentTierCode;
        this.cumulativeSpend = this.cumulativeSpend.add(amount);
        this.currentTierCode = LevelResolver.resolve(tiersAscending, this.cumulativeSpend).getCode();
        if (!this.currentTierCode.equals(previousCode)) {
            this.tierUpgradedAt = OffsetDateTime.now();
            return true;
        }
        return false;
    }

    /**
     * 退款场景：扣减累计消费（floor 在 0），允许降级——和 accrueSpend 的单调性要求不同，
     * 这是对"等级只增不减"假设的一次刻意松动，只在退款这一条路径上生效。
     * @return 等级是否发生变化（升级或降级）
     */
    public boolean reverseSpend(BigDecimal amount, List<MembershipTierDefinition> tiersAscending) {
        String previousCode = this.currentTierCode;
        this.cumulativeSpend = this.cumulativeSpend.subtract(amount).max(BigDecimal.ZERO);
        this.currentTierCode = LevelResolver.resolve(tiersAscending, this.cumulativeSpend).getCode();
        return !this.currentTierCode.equals(previousCode);
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

    public String getCurrentTierCode() {
        return currentTierCode;
    }

    public BigDecimal getCumulativeSpend() {
        return cumulativeSpend;
    }

    public OffsetDateTime getTierUpgradedAt() {
        return tierUpgradedAt;
    }
}
