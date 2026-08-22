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
 * 用户持久化对象
 * @author mystikos
 */
@Data
@Schema(description = "用户持久化对象")
@TableName("identity_user")
public class UserPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法生成）
     */
    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 手机号，与邮箱二选一，非空值全局唯一
     */
    @Schema(description = "手机号")
    @TableField("phone")
    private String phone;

    /**
     * 邮箱，与手机号二选一，非空值全局唯一
     */
    @Schema(description = "邮箱")
    @TableField("email")
    private String email;

    /**
     * 密码哈希（BCrypt）。免密账号（仅验证码/第三方登录）可为空
     */
    @Schema(description = "密码哈希")
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    @TableField("nickname")
    private String nickname;

    /**
     * 是否匿名上榜
     */
    @Schema(description = "是否匿名上榜")
    @TableField("privacy_anonymous")
    private Boolean privacyAnonymous;

    /**
     * 账号状态：ACTIVE/DISABLED/BANNED
     */
    @Schema(description = "账号状态")
    @TableField("status")
    private String status;

    /**
     * 会员等级数值，仅当拥有 MEMBER 角色时有意义
     */
    @Schema(description = "会员等级数值")
    @TableField("membership_tier_level")
    private Integer membershipTierLevel;

    /**
     * 会员等级编码，仅当拥有 MEMBER 角色时有意义
     */
    @Schema(description = "会员等级编码")
    @TableField("membership_tier_code")
    private String membershipTierCode;

    /**
     * 注册时间
     */
    @Schema(description = "注册时间")
    @TableField("created_at")
    private OffsetDateTime createdAt;
}
