package com.mystikos.common.security.persistence;

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
 * Spring Security 免鉴权（permitAll）路径白名单持久化对象。
 * 只在 {@link com.mystikos.common.security.SecurityConfig} 启动组装
 * SecurityFilterChain 时整表读一次（{@code enabled = true}），不是运行时热更新。
 * @author mystikos
 */
@Data
@Schema(description = "Spring Security 白名单路径持久化对象")
@TableName("security_whitelist_path")
public class SecurityWhitelistPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Ant 风格路径匹配模式，如 "/api/v1/auth/login"、"/api/v1/auth/oauth/*&#47;login"
     */
    @Schema(description = "路径匹配模式（Ant 风格）")
    @TableField("path_pattern")
    private String pathPattern;

    /**
     * HTTP 方法，如 GET/POST；为空表示不限方法
     */
    @Schema(description = "HTTP 方法，为空表示不限方法")
    @TableField("http_method")
    private String httpMethod;

    @Schema(description = "说明")
    @TableField("description")
    private String description;

    @Schema(description = "是否启用")
    @TableField("enabled")
    private Boolean enabled;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private OffsetDateTime createdAt;
}
