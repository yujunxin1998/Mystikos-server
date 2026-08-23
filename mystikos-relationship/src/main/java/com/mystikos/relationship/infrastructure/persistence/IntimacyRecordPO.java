package com.mystikos.relationship.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * patronId+companionId 是业务上的复合键，但 MyBatis-Plus 对复合主键支持不友好，
 * 落库时额外加一个自增代理主键，唯一性靠 (patron_id, companion_id) 的 UNIQUE 约束保证。
 */
@Data
@Schema(description = "亲密度记录持久化对象")
@TableName("relationship_intimacy_record")
public class IntimacyRecordPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "代理主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "老板用户ID")
    @TableField("patron_id")
    private Long patronId;

    @Schema(description = "陪玩用户ID")
    @TableField("companion_id")
    private Long companionId;

    @Schema(description = "亲密度阶段：0-4")
    @TableField("stage")
    private Integer stage;

    @Schema(description = "累计互动进度值")
    @TableField("progress_value")
    private BigDecimal progressValue;

    @Schema(description = "最后互动时间")
    @TableField("last_interaction_at")
    private OffsetDateTime lastInteractionAt;
}
