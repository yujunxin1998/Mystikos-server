package com.mystikos.systemoperation.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Schema(description = "系统文档持久化对象")
@TableName("sysop_document")
public class SystemDocumentPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "文档编码，自由字符串，如 TERMS_OF_SERVICE")
    @TableField("code")
    private String code;

    @Schema(description = "标题")
    @TableField("title")
    private String title;

    @Schema(description = "正文内容")
    @TableField("content")
    private String content;

    @Schema(description = "版本号，每次更新自增")
    @TableField("version")
    private Integer version;

    @Schema(description = "最近一次更新人ID")
    @TableField("updated_by")
    private String updatedBy;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private OffsetDateTime createdAt;

    @Schema(description = "最近一次更新时间")
    @TableField("updated_at")
    private OffsetDateTime updatedAt;
}
