package com.mystikos.identity.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 打手（陪玩）后台管理扩展资料，一对一挂在 {@link User} 上（{@code userId} 是
 * {@code identity_user.id}）——"陪玩"就是拥有 {@link Role#COMPANION} 角色的 User，
 * 不是独立的账号体系，这里只是给运营后台补充级别/技能标签/时薪/银行信息这些字段。
 * 级别、技能标签目前业务没有固定的枚举/目录，先用自由文本承接。
 * 业绩统计（接单数/流水/评分）还没接 Booking/Leaderboard 数据源，见 CompanionApplicationService 的占位实现。
 */
public class CompanionProfile {

    private final Long userId;
    private String level;
    private List<String> skillTags;
    private BigDecimal hourlyRate;
    private CompanionStatus status;
    private String idCardNo;
    private String bankAccountName;
    private String bankAccountNo;
    private String bankName;
    private final OffsetDateTime createdAt;

    private CompanionProfile(Long userId, String level, List<String> skillTags, BigDecimal hourlyRate,
                              CompanionStatus status, String idCardNo, String bankAccountName,
                              String bankAccountNo, String bankName, OffsetDateTime createdAt) {
        this.userId = userId;
        this.level = level;
        this.skillTags = skillTags == null ? List.of() : List.copyOf(skillTags);
        this.hourlyRate = hourlyRate;
        this.status = status == null ? CompanionStatus.OFFLINE : status;
        this.idCardNo = idCardNo;
        this.bankAccountName = bankAccountName;
        this.bankAccountNo = bankAccountNo;
        this.bankName = bankName;
        this.createdAt = createdAt;
    }

    public static CompanionProfile create(Long userId, String level, List<String> skillTags, BigDecimal hourlyRate,
                                           CompanionStatus status, String idCardNo, String bankAccountName,
                                           String bankAccountNo, String bankName) {
        return new CompanionProfile(userId, level, skillTags, hourlyRate, status, idCardNo, bankAccountName,
                bankAccountNo, bankName, OffsetDateTime.now());
    }

    /** 从持久化数据重建，仅供仓储实现调用。 */
    public static CompanionProfile restore(Long userId, String level, List<String> skillTags, BigDecimal hourlyRate,
                                            CompanionStatus status, String idCardNo, String bankAccountName,
                                            String bankAccountNo, String bankName, OffsetDateTime createdAt) {
        return new CompanionProfile(userId, level, skillTags, hourlyRate, status, idCardNo, bankAccountName,
                bankAccountNo, bankName, createdAt);
    }

    public void update(String level, List<String> skillTags, BigDecimal hourlyRate, CompanionStatus status,
                        String idCardNo, String bankAccountName, String bankAccountNo, String bankName) {
        this.level = level;
        this.skillTags = skillTags == null ? List.of() : List.copyOf(skillTags);
        this.hourlyRate = hourlyRate;
        this.status = status == null ? this.status : status;
        this.idCardNo = idCardNo;
        this.bankAccountName = bankAccountName;
        this.bankAccountNo = bankAccountNo;
        this.bankName = bankName;
    }

    public Long getUserId() {
        return userId;
    }

    public String getLevel() {
        return level;
    }

    public List<String> getSkillTags() {
        return skillTags;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public CompanionStatus getStatus() {
        return status;
    }

    public String getIdCardNo() {
        return idCardNo;
    }

    public String getBankAccountName() {
        return bankAccountName;
    }

    public String getBankAccountNo() {
        return bankAccountNo;
    }

    public String getBankName() {
        return bankName;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
