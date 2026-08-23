package com.mystikos.identity.infrastructure.persistence;

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
 * 打手（陪玩）后台管理扩展资料持久化对象。主键是 identity_user.id 本身（一对一），
 * 不用雪花算法生成——这是外键值，不是独立生成的代理ID。
 * @author mystikos
 */
@Data
@Schema(description = "打手（陪玩）资料持久化对象")
@TableName("identity_companion_profile")
public class CompanionProfilePO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID，即 identity_user.id")
    @TableId(type = IdType.INPUT)
    @TableField("user_id")
    private Long userId;

    @Schema(description = "级别，自由文本")
    @TableField("level")
    private String level;

    @Schema(description = "小时费率")
    @TableField("hourly_rate")
    private BigDecimal hourlyRate;

    @Schema(description = "接单状态：AVAILABLE/BUSY/OFFLINE")
    @TableField("companion_status")
    private String companionStatus;

    @Schema(description = "身份状态：ACTIVE/REVOKED")
    @TableField("identity_status")
    private String identityStatus;

    @Schema(description = "身份证号（明文存储）")
    @TableField("id_card_no")
    private String idCardNo;

    @Schema(description = "银行开户人姓名")
    @TableField("bank_account_name")
    private String bankAccountName;

    @Schema(description = "银行卡号（明文存储）")
    @TableField("bank_account_no")
    private String bankAccountNo;

    @Schema(description = "开户行")
    @TableField("bank_name")
    private String bankName;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private OffsetDateTime createdAt;
}
