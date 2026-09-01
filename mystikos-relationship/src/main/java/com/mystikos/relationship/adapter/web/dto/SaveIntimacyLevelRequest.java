package com.mystikos.relationship.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "新增/更新亲密度等级请求")
public class SaveIntimacyLevelRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "等级编码")
    @NotBlank
    private String code;

    @Schema(description = "中文展示名")
    @NotBlank
    private String displayNameZh;

    @Schema(description = "英文展示名")
    private String displayNameEn;

    @Schema(description = "进入该等级所需的最低累计进度")
    @NotNull
    private BigDecimal threshold;

    @Schema(description = "权益文案")
    private String perkDescription;

    @Schema(description = "排序位次")
    @NotNull
    private Integer sortOrder;
}
