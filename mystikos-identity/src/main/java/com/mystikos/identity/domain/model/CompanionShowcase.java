package com.mystikos.identity.domain.model;

import java.time.OffsetDateTime;

/**
 * 陪玩名片已发布台账，一对一挂在 userId 上。只存指向当前生效 {@link CompanionShowcaseRevision}
 * 的指针，不复制内容——老板端读取名片时永远只看这张台账指向的 revision，草稿/待审内容不会
 * 提前展示。审核通过时由 {@code CompanionShowcaseApplicationService#review} 调用 {@link #publish}
 * 把指针切过去。
 */
public class CompanionShowcase {

    private final Long userId;
    private Long publishedRevisionId;
    private OffsetDateTime publishedAt;

    private CompanionShowcase(Long userId, Long publishedRevisionId, OffsetDateTime publishedAt) {
        this.userId = userId;
        this.publishedRevisionId = publishedRevisionId;
        this.publishedAt = publishedAt;
    }

    /** 用户还没有任何审核通过记录时的空台账。 */
    public static CompanionShowcase empty(Long userId) {
        return new CompanionShowcase(userId, null, null);
    }

    /** 从持久化数据重建，仅供仓储实现调用。 */
    public static CompanionShowcase restore(Long userId, Long publishedRevisionId, OffsetDateTime publishedAt) {
        return new CompanionShowcase(userId, publishedRevisionId, publishedAt);
    }

    /** 某条 revision 审核通过，台账指针切过去。 */
    public void publish(Long revisionId) {
        this.publishedRevisionId = revisionId;
        this.publishedAt = OffsetDateTime.now();
    }

    public boolean isPublished() {
        return publishedRevisionId != null;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getPublishedRevisionId() {
        return publishedRevisionId;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }
}
