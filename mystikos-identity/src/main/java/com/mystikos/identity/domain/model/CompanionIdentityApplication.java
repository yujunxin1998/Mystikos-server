package com.mystikos.identity.domain.model;

import com.mystikos.identity.domain.IdentityException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 陪玩身份申请。用户（已是 {@link Role#MEMBER}）提交申请，管理员线下考核后在后台
 * 修改状态、录入考核结果。考核过程本身不进系统，只有申请中/考核中两个状态之间的
 * 流转和最终审核结果需要落库。
 * <p>
 * 联系方式（手机号+国家区号，或邮箱）是申请专用的联系渠道，不等同于账号本身绑定的
 * {@link User#getPhone()}/{@link User#getEmail()}——同一用户完全可以留一个不同的
 * 联系方式给考核用。至少要填其中一种，见 {@link #submit}。
 */
public class CompanionIdentityApplication {

    private Long id;
    private final Long userId;
    private String realName;
    private Gender gender;
    private LocalDate birthDate;
    private String selfIntro;
    private String gameNickname;
    private String gameRankProofObjectKey;
    private final Set<Long> tagIds;
    private String contactCountryCode;
    private String contactPhone;
    private String contactEmail;
    private CompanionIdentityApplicationStatus status;
    private Long reviewerId;
    private AssessmentResult reviewResult;
    private String reviewComment;
    private OffsetDateTime reviewedAt;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private CompanionIdentityApplication(Long id, Long userId, String realName, Gender gender, LocalDate birthDate,
                                          String selfIntro, String gameNickname, String gameRankProofObjectKey,
                                          Set<Long> tagIds, String contactCountryCode, String contactPhone,
                                          String contactEmail, CompanionIdentityApplicationStatus status,
                                          Long reviewerId, AssessmentResult reviewResult, String reviewComment,
                                          OffsetDateTime reviewedAt, OffsetDateTime createdAt,
                                          OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.realName = realName;
        this.gender = gender == null ? Gender.UNDISCLOSED : gender;
        this.birthDate = birthDate;
        this.selfIntro = selfIntro;
        this.gameNickname = gameNickname;
        this.gameRankProofObjectKey = gameRankProofObjectKey;
        this.tagIds = tagIds == null ? new HashSet<>() : new HashSet<>(tagIds);
        this.contactCountryCode = contactCountryCode;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.status = status;
        this.reviewerId = reviewerId;
        this.reviewResult = reviewResult;
        this.reviewComment = reviewComment;
        this.reviewedAt = reviewedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 提交新申请。联系方式必须至少填一种：手机号（连同国家区号）或邮箱；
     * 二者都不填、或者只填了手机号没填国家区号，都视为联系方式不完整。
     */
    public static CompanionIdentityApplication submit(Long userId, String realName, Gender gender,
                                                        LocalDate birthDate, String selfIntro, String gameNickname,
                                                        String gameRankProofObjectKey, Set<Long> tagIds,
                                                        String contactCountryCode, String contactPhone,
                                                        String contactEmail) {
        boolean hasPhone = contactPhone != null && !contactPhone.isBlank();
        boolean hasEmail = contactEmail != null && !contactEmail.isBlank();
        if (!hasPhone && !hasEmail) {
            throw IdentityException.companionApplicationContactRequired();
        }
        if (hasPhone && (contactCountryCode == null || contactCountryCode.isBlank())) {
            throw IdentityException.companionApplicationContactRequired();
        }
        OffsetDateTime now = OffsetDateTime.now();
        return new CompanionIdentityApplication(null, userId, realName, gender, birthDate, selfIntro, gameNickname,
                gameRankProofObjectKey, tagIds, hasPhone ? contactCountryCode : null, hasPhone ? contactPhone : null,
                hasEmail ? contactEmail : null, CompanionIdentityApplicationStatus.SUBMITTED, null, null, null, null,
                now, now);
    }

    /** 从持久化数据重建，仅供仓储实现调用。 */
    public static CompanionIdentityApplication restore(Long id, Long userId, String realName, Gender gender,
                                                         LocalDate birthDate, String selfIntro, String gameNickname,
                                                         String gameRankProofObjectKey, Set<Long> tagIds,
                                                         String contactCountryCode, String contactPhone,
                                                         String contactEmail,
                                                         CompanionIdentityApplicationStatus status, Long reviewerId,
                                                         AssessmentResult reviewResult, String reviewComment,
                                                         OffsetDateTime reviewedAt, OffsetDateTime createdAt,
                                                         OffsetDateTime updatedAt) {
        return new CompanionIdentityApplication(id, userId, realName, gender, birthDate, selfIntro, gameNickname,
                gameRankProofObjectKey, tagIds, contactCountryCode, contactPhone, contactEmail, status, reviewerId,
                reviewResult, reviewComment, reviewedAt, createdAt, updatedAt);
    }

    /** 管理员开始考核：仅状态流转，考核过程本身线下进行，不需要额外数据。 */
    public void startAssessment() {
        if (status != CompanionIdentityApplicationStatus.SUBMITTED) {
            throw IdentityException.companionApplicationInvalidStatusTransition(status);
        }
        this.status = CompanionIdentityApplicationStatus.IN_ASSESSMENT;
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * 录入考核结果。考核人和考核结果必填，审核意见可为空；结果为 PASS/FAIL 直接决定
     * 最终状态是 APPROVED/REJECTED。允许从「申请中」直接录入结果（跳过「考核中」这一步），
     * 也允许从「考核中」录入，但已经是终态（APPROVED/REJECTED）的申请不能再次审核。
     */
    public void review(Long reviewerId, AssessmentResult result, String comment) {
        if (status != CompanionIdentityApplicationStatus.SUBMITTED
                && status != CompanionIdentityApplicationStatus.IN_ASSESSMENT) {
            throw IdentityException.companionApplicationInvalidStatusTransition(status);
        }
        if (reviewerId == null) {
            throw IdentityException.companionApplicationReviewerRequired();
        }
        if (result == null) {
            throw IdentityException.companionApplicationResultRequired();
        }
        this.reviewerId = reviewerId;
        this.reviewResult = result;
        this.reviewComment = comment;
        this.reviewedAt = OffsetDateTime.now();
        this.status = result == AssessmentResult.PASS
                ? CompanionIdentityApplicationStatus.APPROVED
                : CompanionIdentityApplicationStatus.REJECTED;
        this.updatedAt = this.reviewedAt;
    }

    public Long getId() {
        return id;
    }

    /** 仅供仓储实现在插入后回填生成的主键。 */
    public void assignId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getRealName() {
        return realName;
    }

    public Gender getGender() {
        return gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getSelfIntro() {
        return selfIntro;
    }

    public String getGameNickname() {
        return gameNickname;
    }

    public String getGameRankProofObjectKey() {
        return gameRankProofObjectKey;
    }

    public Set<Long> getTagIds() {
        return Collections.unmodifiableSet(tagIds);
    }

    public String getContactCountryCode() {
        return contactCountryCode;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public CompanionIdentityApplicationStatus getStatus() {
        return status;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public AssessmentResult getReviewResult() {
        return reviewResult;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public OffsetDateTime getReviewedAt() {
        return reviewedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
