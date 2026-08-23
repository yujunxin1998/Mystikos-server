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
 * 陪玩名片已发布台账持久化对象，主键就是 identity_user.id（一对一，不用独立代理键），
 * 同 {@link CompanionProfilePO}。
 */
@Data
@Schema(description = "陪玩名片已发布台账持久化对象")
@TableName("identity_companion_showcase")
public class CompanionShowcasePO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "陪玩用户ID，即 identity_user.id")
    @TableId(type = IdType.INPUT)
    @TableField("user_id")
    private Long userId;

    @Schema(description = "当前生效的 revision ID，未曾发布过时为空")
    @TableField("published_revision_id")
    private Long publishedRevisionId;

    @Schema(description = "发布时间")
    @TableField("published_at")
    private OffsetDateTime publishedAt;
}
