package com.mystikos.identity.domain.model;

import com.mystikos.identity.domain.IdentityException;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 陪玩名片的一次编辑/提交记录。同一用户会有多条历史记录（每次编辑要么就地更新当前草稿，
 * 要么在上一条是终态时新开一条，见 {@code CompanionShowcaseApplicationService#saveDraft}）。
 * 只有审核通过（{@link CompanionShowcaseRevisionStatus#APPROVED}）时，{@link CompanionShowcase}
 * 台账才会把 publishedRevisionId 指向这条记录，老板端读取的是台账指针指向的内容，草稿/待审内容
 * 不会被提前看到。
 */
public class CompanionShowcaseRevision {

    private Long id;
    private final Long userId;
    private String bio;
    private Set<Long> tagIds;
    private List<String> photoObjectKeys;
    private List<String> videoObjectKeys;
    private List<String> audioObjectKeys;
    private CompanionShowcaseRevisionStatus status;
    private Long reviewerId;
    private String reviewComment;
    private OffsetDateTime reviewedAt;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private CompanionShowcaseRevision(Long id, Long userId, String bio, Set<Long> tagIds,
                                       List<String> photoObjectKeys, List<String> videoObjectKeys,
                                       List<String> audioObjectKeys, CompanionShowcaseRevisionStatus status,
                                       Long reviewerId, String reviewComment, OffsetDateTime reviewedAt,
                                       OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.bio = bio;
        this.tagIds = tagIds == null ? new HashSet<>() : new HashSet<>(tagIds);
        this.photoObjectKeys = photoObjectKeys == null ? List.of() : List.copyOf(photoObjectKeys);
        this.videoObjectKeys = videoObjectKeys == null ? List.of() : List.copyOf(videoObjectKeys);
        this.audioObjectKeys = audioObjectKeys == null ? List.of() : List.copyOf(audioObjectKeys);
        this.status = status;
        this.reviewerId = reviewerId;
        this.reviewComment = reviewComment;
        this.reviewedAt = reviewedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 新开一条空草稿：陪玩第一次编辑，或者上一条记录已经是终态（APPROVED/REJECTED）时用。 */
    public static CompanionShowcaseRevision draft(Long userId) {
        OffsetDateTime now = OffsetDateTime.now();
        return new CompanionShowcaseRevision(null, userId, null, Set.of(), List.of(), List.of(), List.of(),
                CompanionShowcaseRevisionStatus.DRAFT, null, null, null, now, now);
    }

    /** 从持久化数据重建，仅供仓储实现调用。 */
    public static CompanionShowcaseRevision restore(Long id, Long userId, String bio, Set<Long> tagIds,
                                                      List<String> photoObjectKeys, List<String> videoObjectKeys,
                                                      List<String> audioObjectKeys,
                                                      CompanionShowcaseRevisionStatus status, Long reviewerId,
                                                      String reviewComment, OffsetDateTime reviewedAt,
                                                      OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        return new CompanionShowcaseRevision(id, userId, bio, tagIds, photoObjectKeys, videoObjectKeys,
                audioObjectKeys, status, reviewerId, reviewComment, reviewedAt, createdAt, updatedAt);
    }

    /** 更新草稿内容：只有编辑中（DRAFT）能改，待审核/终态都要先走完流程或重新开草稿。 */
    public void updateContent(String bio, Set<Long> tagIds, List<String> photoObjectKeys,
                               List<String> videoObjectKeys, List<String> audioObjectKeys) {
        if (status != CompanionShowcaseRevisionStatus.DRAFT) {
            throw IdentityException.companionShowcaseInvalidStatusTransition(status);
        }
        this.bio = bio;
        this.tagIds = tagIds == null ? new HashSet<>() : new HashSet<>(tagIds);
        this.photoObjectKeys = photoObjectKeys == null ? List.of() : List.copyOf(photoObjectKeys);
        this.videoObjectKeys = videoObjectKeys == null ? List.of() : List.copyOf(videoObjectKeys);
        this.audioObjectKeys = audioObjectKeys == null ? List.of() : List.copyOf(audioObjectKeys);
        this.updatedAt = OffsetDateTime.now();
    }

    /** 提交审核：草稿至少要有一张照片和一个游戏标签，否则名片没法展示。 */
    public void submit() {
        if (status != CompanionShowcaseRevisionStatus.DRAFT) {
            throw IdentityException.companionShowcaseInvalidStatusTransition(status);
        }
        if (photoObjectKeys.isEmpty() || tagIds.isEmpty()) {
            throw IdentityException.companionShowcaseIncomplete();
        }
        this.status = CompanionShowcaseRevisionStatus.PENDING_REVIEW;
        this.updatedAt = OffsetDateTime.now();
    }

    /** 审核通过：台账指针的更新由 {@code CompanionShowcase#publish} 另外完成，这里只翻转自身状态。 */
    public void approve(Long reviewerId, String comment) {
        if (status != CompanionShowcaseRevisionStatus.PENDING_REVIEW) {
            throw IdentityException.companionShowcaseInvalidStatusTransition(status);
        }
        this.reviewerId = reviewerId;
        this.reviewComment = comment;
        this.reviewedAt = OffsetDateTime.now();
        this.status = CompanionShowcaseRevisionStatus.APPROVED;
        this.updatedAt = this.reviewedAt;
    }

    /** 审核不通过：必须说明原因，陪玩下次编辑时会看到。 */
    public void reject(Long reviewerId, String reason) {
        if (status != CompanionShowcaseRevisionStatus.PENDING_REVIEW) {
            throw IdentityException.companionShowcaseInvalidStatusTransition(status);
        }
        if (reason == null || reason.isBlank()) {
            throw IdentityException.companionShowcaseReviewReasonRequired();
        }
        this.reviewerId = reviewerId;
        this.reviewComment = reason;
        this.reviewedAt = OffsetDateTime.now();
        this.status = CompanionShowcaseRevisionStatus.REJECTED;
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

    public String getBio() {
        return bio;
    }

    public Set<Long> getTagIds() {
        return Collections.unmodifiableSet(tagIds);
    }

    public List<String> getPhotoObjectKeys() {
        return photoObjectKeys;
    }

    public List<String> getVideoObjectKeys() {
        return videoObjectKeys;
    }

    public List<String> getAudioObjectKeys() {
        return audioObjectKeys;
    }

    public CompanionShowcaseRevisionStatus getStatus() {
        return status;
    }

    public Long getReviewerId() {
        return reviewerId;
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
