package com.mystikos.identity.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 陪玩身份申请持久化对象。主键是雪花算法生成的代理ID（跟 {@code identity_user.id} 不是
 * 同一个值——同一用户可以有多条历史申请记录，见 {@link CompanionIdentityApplicationRepositoryImpl}）。
 */
@Data
@Schema(description = "陪玩身份申请持久化对象")
@TableName("identity_companion_application")
public class CompanionIdentityApplicationPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "申请ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "申请人用户ID，即 identity_user.id")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "真实姓名")
    @TableField("real_name")
    private String realName;

    @Schema(description = "性别")
    @TableField("gender")
    private String gender;

    @Schema(description = "出生日期")
    @TableField("birth_date")
    private LocalDate birthDate;

    @Schema(description = "自我介绍/擅长说明")
    @TableField("self_intro")
    private String selfIntro;

    @Schema(description = "游戏昵称")
    @TableField("game_nickname")
    private String gameNickname;

    @Schema(description = "段位截图等游戏相关资料，对象存储键")
    @TableField("game_rank_proof_object_key")
    private String gameRankProofObjectKey;

    @Schema(description = "联系手机号国家区号，如 +86")
    @TableField("contact_country_code")
    private String contactCountryCode;

    @Schema(description = "联系手机号")
    @TableField("contact_phone")
    private String contactPhone;

    @Schema(description = "联系邮箱")
    @TableField("contact_email")
    private String contactEmail;

    @Schema(description = "申请状态：SUBMITTED/IN_ASSESSMENT/APPROVED/REJECTED")
    @TableField("status")
    private String status;

    @Schema(description = "考核人用户ID")
    @TableField("reviewer_id")
    private Long reviewerId;

    @Schema(description = "考核结果：PASS/FAIL")
    @TableField("review_result")
    private String reviewResult;

    @Schema(description = "审核意见")
    @TableField("review_comment")
    private String reviewComment;

    @Schema(description = "考核时间")
    @TableField("reviewed_at")
    private OffsetDateTime reviewedAt;

    @Schema(description = "提交时间")
    @TableField("created_at")
    private OffsetDateTime createdAt;

    @Schema(description = "最后更新时间")
    @TableField("updated_at")
    private OffsetDateTime updatedAt;
}
