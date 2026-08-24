package com.mystikos.identity.infrastructure.persistence;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * identity_companion_showcase + identity_companion_showcase_revision + identity_user 三表联查
 * （老板浏览目录用）的结果行，纯查询用，不是 MyBatis-Plus 实体，同 {@link CompanionShowcaseRevisionRowPO}。
 */
@Data
public class CompanionShowcasePublicRowPO {
    private Long revisionId;
    private Long userId;
    private String nickname;
    private String avatarObjectKey;
    private String bio;
    private String tagline;
    private String availability;
    private OffsetDateTime publishedAt;
}
