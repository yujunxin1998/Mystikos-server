package com.mystikos.leaderboard.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "老板守护值统计持久化对象")
@TableName("leaderboard_patron_stat")
public class PatronGuardStatPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "代理主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "老板用户ID")
    @TableField("patron_id")
    private Long patronId;

    @Schema(description = "累计守护值")
    @TableField("guard_value")
    private BigDecimal guardValue;
}
