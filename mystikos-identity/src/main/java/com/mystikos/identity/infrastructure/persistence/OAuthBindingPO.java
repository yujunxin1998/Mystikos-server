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
 * 第三方登录绑定持久化对象
 * @author mystikos
 */
@Data
@Schema(description = "第三方登录绑定持久化对象")
@TableName("identity_oauth_binding")
public class OAuthBindingPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联的用户ID
     */
    @Schema(description = "关联的用户ID")
    @TableField("user_id")
    private Long userId;

    /**
     * 第三方供应商编码（如 discord、wechat），字符串不用枚举，接入新供应商不改表结构
     */
    @Schema(description = "第三方供应商编码")
    @TableField("provider")
    private String provider;

    /**
     * 第三方系统里的用户ID
     */
    @Schema(description = "第三方系统里的用户ID")
    @TableField("provider_user_id")
    private String providerUserId;

    /**
     * 绑定时间
     */
    @Schema(description = "绑定时间")
    @TableField("bound_at")
    private OffsetDateTime boundAt;
}
