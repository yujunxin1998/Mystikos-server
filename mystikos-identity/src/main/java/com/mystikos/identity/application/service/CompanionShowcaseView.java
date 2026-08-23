package com.mystikos.identity.application.service;

import com.mystikos.identity.domain.model.CompanionShowcaseRevisionStatus;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 陪玩名片草稿/提交记录视图，陪玩自查（{@code GET /me}）和管理员审核队列
 * （{@code GET /manage/companion-showcases}）复用同一个视图，同
 * {@link CompanionIdentityApplicationView} 的做法。
 */
public record CompanionShowcaseView(
        Long id,
        Long userId,
        String applicantNickname,
        String applicantPhone,
        String applicantEmail,
        CompanionShowcaseRevisionStatus status,
        String bio,
        List<TagView> tags,
        List<String> photoUrls,
        List<String> videoUrls,
        List<String> audioUrls,
        Long reviewerId,
        String reviewerNickname,
        String reviewComment,
        OffsetDateTime reviewedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        boolean published,
        OffsetDateTime publishedAt
) {
}
