package com.mystikos.identity.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * 陪玩名片草稿/提交记录持久化对象。主键是雪花算法生成的代理ID，同一用户可以有多条历史记录，
 * 见 {@link CompanionShowcaseRevisionRepositoryImpl}。
 */
@Data
@Schema(description = "陪玩名片草稿/提交记录持久化对象")
@TableName("identity_companion_showcase_revision")
public class CompanionShowcaseRevisionPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "陪玩用户ID，即 identity_user.id")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "自我介绍")
    @TableField("bio")
    private String bio;

    @Schema(description = "状态：DRAFT/PENDING_REVIEW/APPROVED/REJECTED")
    @TableField("status")
    private String status;

    @Schema(description = "审核人用户ID")
    @TableField("reviewer_id")
    private Long reviewerId;

    @Schema(description = "审核意见/驳回原因")
    @TableField("review_comment")
    private String reviewComment;

    @Schema(description = "审核时间")
    @TableField("reviewed_at")
    private OffsetDateTime reviewedAt;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private OffsetDateTime createdAt;

    @Schema(description = "最后更新时间")
    @TableField("updated_at")
    private OffsetDateTime updatedAt;
}
