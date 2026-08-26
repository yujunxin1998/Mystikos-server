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

/** 每次 {@code SystemDocument} 内容变化的只追加快照，供后台查看修订历史。 */
@Data
@Schema(description = "系统文档修订快照持久化对象")
@TableName("sysop_document_revision")
public class SystemDocumentRevisionPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "所属文档编码")
    @TableField("document_code")
    private String documentCode;

    @Schema(description = "该快照对应的版本号")
    @TableField("version")
    private Integer version;

    @Schema(description = "标题快照")
    @TableField("title")
    private String title;

    @Schema(description = "正文内容快照")
    @TableField("content")
    private String content;

    @Schema(description = "本次更新人ID")
    @TableField("updated_by")
    private String updatedBy;

    @Schema(description = "本次更新时间")
    @TableField("created_at")
    private OffsetDateTime createdAt;
}
