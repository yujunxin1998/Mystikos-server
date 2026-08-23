package com.mystikos.identity.application.service;

import java.time.OffsetDateTime;
import java.util.List;

/** 老板端浏览陪玩名片用，只有已发布内容，没有任何审核相关字段。 */
public record CompanionShowcasePublicView(
        Long userId,
        String nickname,
        String avatarUrl,
        String bio,
        List<TagView> tags,
        List<String> photoUrls,
        List<String> videoUrls,
        List<String> audioUrls,
        OffsetDateTime publishedAt
) {
}
