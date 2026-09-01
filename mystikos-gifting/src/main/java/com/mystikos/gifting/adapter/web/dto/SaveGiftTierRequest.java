package com.mystikos.gifting.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "新增/更新礼物档位请求")
public class SaveGiftTierRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "档位编码")
    @NotBlank
    private String code;

    @Schema(description = "展示名")
    @NotBlank
    private String displayName;

    @Schema(description = "英文展示名")
    private String displayNameEn;

    @Schema(description = "亲密度倍率")
    @NotNull
    @Positive
    private BigDecimal multiplier;

    @Schema(description = "排序位次")
    @NotNull
    private Integer sortOrder;

    @Schema(description = "是否启用，缺省为 true")
    private Boolean active;
}
