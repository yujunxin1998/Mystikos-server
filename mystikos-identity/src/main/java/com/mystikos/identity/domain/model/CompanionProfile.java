package com.mystikos.identity.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 打手（陪玩）身份台账，一对一挂在 {@link User} 上（{@code userId} 是
 * {@code identity_user.id}）——"陪玩"就是拥有 {@link Role#COMPANION} 角色的 User，
 * 不是独立的账号体系，这里只是给运营后台补充级别/擅长游戏标签/时薪/银行信息这些字段。
 * {@code identityStatus} 是这份台账本身的身份状态（ACTIVE/REVOKED），跟角色分配保持
 * 同步的职责在 {@code CompanionApplicationService} 的 grant/revoke 方法里，聚合本身
 * 不认识 {@link User} 仓储。级别目前业务没有固定的枚举/目录，先用自由文本承接。
 * 业绩统计（接单数/流水/评分）还没接 Booking/Leaderboard 数据源，见 CompanionApplicationService 的占位实现。
 */
public class CompanionProfile {

    private final Long userId;
    private String level;
    private Set<Long> tagIds;
    private BigDecimal hourlyRate;
    private CompanionStatus status;
    private CompanionIdentityStatus identityStatus;
    private String idCardNo;
    private String bankAccountName;
    private String bankAccountNo;
    private String bankName;
    private final OffsetDateTime createdAt;

    private CompanionProfile(Long userId, String level, Set<Long> tagIds, BigDecimal hourlyRate,
                              CompanionStatus status, CompanionIdentityStatus identityStatus, String idCardNo,
                              String bankAccountName, String bankAccountNo, String bankName,
                              OffsetDateTime createdAt) {
        this.userId = userId;
        this.level = level;
        this.tagIds = tagIds == null ? new HashSet<>() : new HashSet<>(tagIds);
        this.hourlyRate = hourlyRate;
        this.status = status == null ? CompanionStatus.OFFLINE : status;
        this.identityStatus = identityStatus == null ? CompanionIdentityStatus.ACTIVE : identityStatus;
        this.idCardNo = idCardNo;
        this.bankAccountName = bankAccountName;
        this.bankAccountNo = bankAccountNo;
        this.bankName = bankName;
        this.createdAt = createdAt;
    }

    /** 首次开通陪玩身份，新建台账，状态默认 ACTIVE。 */
    public static CompanionProfile create(Long userId, String level, Set<Long> tagIds, BigDecimal hourlyRate,
                                           CompanionStatus status, String idCardNo, String bankAccountName,
                                           String bankAccountNo, String bankName) {
        return new CompanionProfile(userId, level, tagIds, hourlyRate, status, CompanionIdentityStatus.ACTIVE,
                idCardNo, bankAccountName, bankAccountNo, bankName, OffsetDateTime.now());
    }

    /** 从持久化数据重建，仅供仓储实现调用。 */
    public static CompanionProfile restore(Long userId, String level, Set<Long> tagIds, BigDecimal hourlyRate,
                                            CompanionStatus status, CompanionIdentityStatus identityStatus,
                                            String idCardNo, String bankAccountName, String bankAccountNo,
                                            String bankName, OffsetDateTime createdAt) {
        return new CompanionProfile(userId, level, tagIds, hourlyRate, status, identityStatus, idCardNo,
                bankAccountName, bankAccountNo, bankName, createdAt);
    }

    public void update(String level, Set<Long> tagIds, BigDecimal hourlyRate, CompanionStatus status,
                        String idCardNo, String bankAccountName, String bankAccountNo, String bankName) {
        this.level = level;
        this.tagIds = tagIds == null ? new HashSet<>() : new HashSet<>(tagIds);
        this.hourlyRate = hourlyRate;
        this.status = status == null ? this.status : status;
        this.idCardNo = idCardNo;
        this.bankAccountName = bankAccountName;
        this.bankAccountNo = bankAccountNo;
        this.bankName = bankName;
    }

    /** 收回陪玩身份：软删除，级别/时薪/银行信息等运营资料原样保留，方便重新开通时复用。 */
    public void revoke() {
        this.identityStatus = CompanionIdentityStatus.REVOKED;
    }

    /**
     * 收回后再次授予身份（比如重新申请陪玩并审核通过）：只翻转身份状态、覆盖擅长游戏标签，
     * 级别/时薪/银行信息等运营资料保持不动——那些是运营后台单独维护的字段，申请表单不会带这些数据。
     */
    public void grant(Set<Long> tagIds) {
        this.tagIds = tagIds == null ? new HashSet<>() : new HashSet<>(tagIds);
        this.identityStatus = CompanionIdentityStatus.ACTIVE;
    }

    public Long getUserId() {
        return userId;
    }

    public String getLevel() {
        return level;
    }

    public Set<Long> getTagIds() {
        return Collections.unmodifiableSet(tagIds);
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public CompanionStatus getStatus() {
        return status;
    }

    public CompanionIdentityStatus getIdentityStatus() {
        return identityStatus;
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
