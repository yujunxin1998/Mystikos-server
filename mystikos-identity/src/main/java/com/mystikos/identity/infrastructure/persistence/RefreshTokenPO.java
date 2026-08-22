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
 * Refresh Token 持久化对象
 * @author mystikos
 */
@Data
@Schema(description = "Refresh Token持久化对象")
@TableName("identity_refresh_token")
public class RefreshTokenPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属用户ID
     */
    @Schema(description = "所属用户ID")
    @TableField("user_id")
    private Long userId;

    /**
     * Token 哈希（SHA-256），不落明文
     */
    @Schema(description = "Token哈希")
    @TableField("token_hash")
    private String tokenHash;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间")
    @TableField("expires_at")
    private OffsetDateTime expiresAt;

    /**
     * 吊销时间，为空表示未吊销
     */
    @Schema(description = "吊销时间")
    @TableField("revoked_at")
    private OffsetDateTime revokedAt;
}
