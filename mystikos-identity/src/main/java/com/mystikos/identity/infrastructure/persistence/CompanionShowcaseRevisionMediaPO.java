package com.mystikos.identity.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 陪玩名片媒体文件持久化对象（照片/视频/语音），一个 revision 下可以有多条，
 * sort_order 保持陪玩排的展示顺序，见 {@link CompanionShowcaseRevisionRepositoryImpl}。
 */
@Data
@Schema(description = "陪玩名片媒体文件持久化对象")
@TableName("identity_companion_showcase_revision_media")
public class CompanionShowcaseRevisionMediaPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "媒体记录ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "所属 revision ID")
    @TableField("revision_id")
    private Long revisionId;

    @Schema(description = "媒体类型：PHOTO/VIDEO/AUDIO")
    @TableField("media_type")
    private String mediaType;

    @Schema(description = "对象存储键")
    @TableField("object_key")
    private String objectKey;

    @Schema(description = "展示顺序，同类型内从0开始")
    @TableField("sort_order")
    private Integer sortOrder;
}
