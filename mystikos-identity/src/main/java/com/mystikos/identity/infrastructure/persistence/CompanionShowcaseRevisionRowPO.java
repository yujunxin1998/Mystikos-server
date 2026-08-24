package com.mystikos.identity.infrastructure.persistence;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * identity_companion_showcase_revision + identity_user 联表查询（按昵称/手机号/邮箱过滤）的结果行，
 * 纯查询用，不是 MyBatis-Plus 实体，同 {@link CompanionRowPO}。
 */
@Data
public class CompanionShowcaseRevisionRowPO {
    private Long id;
    private Long userId;
    private String bio;
    private String tagline;
    private String availability;
    private String status;
    private Long reviewerId;
    private String reviewComment;
    private OffsetDateTime reviewedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
