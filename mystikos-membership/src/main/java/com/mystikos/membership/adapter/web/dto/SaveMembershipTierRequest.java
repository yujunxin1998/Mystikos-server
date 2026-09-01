package com.mystikos.membership.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "新增/更新 VIP 等级请求")
public class SaveMembershipTierRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "等级编码")
    @NotBlank
    private String code;

    @Schema(description = "展示名")
    @NotBlank
    private String displayName;

    @Schema(description = "英文展示名")
    private String displayNameEn;

    @Schema(description = "对外展示的等级数值")
    @NotNull
    private Integer level;

    @Schema(description = "进入该等级所需的最低累计消费")
    @NotNull
    private BigDecimal cumulativeSpendThreshold;

    @Schema(description = "权益文案")
    private String perkDescription;

    @Schema(description = "排序位次")
    @NotNull
    private Integer sortOrder;
}
