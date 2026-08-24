package com.mystikos.identity.domain.model;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * 陪玩名片目录（老板浏览列表）的联表只读投影，只覆盖已发布内容，同
 * {@link CompanionSummary} 的做法——查询/分页在仓储实现里做，这里只是结果形状。
 */
public record CompanionShowcasePublicSummary(
        Long userId,
        String nickname,
        String avatarObjectKey,
        String bio,
        String tagline,
        String availability,
        Set<Long> tagIds,
        String coverPhotoObjectKey,
        OffsetDateTime publishedAt
) {
}
