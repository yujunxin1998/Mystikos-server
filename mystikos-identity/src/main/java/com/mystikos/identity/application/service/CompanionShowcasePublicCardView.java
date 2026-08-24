package com.mystikos.identity.application.service;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * 老板浏览陪玩名片目录（列表）用，比详情页 {@link CompanionShowcasePublicView} 轻——
 * 只带一张封面图（首张照片），不带完整媒体列表，列表页没必要为每张卡片都拉全部照片/视频/语音。
 */
public record CompanionShowcasePublicCardView(
        @JsonSerialize(using = ToStringSerializer.class) Long userId,
        String nickname,
        String avatarUrl,
        String bio,
        String tagline,
        String availability,
        List<TagView> tags,
        String coverPhotoUrl,
        OffsetDateTime publishedAt
) {
}
