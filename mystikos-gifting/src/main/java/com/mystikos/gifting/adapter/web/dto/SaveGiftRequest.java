package com.mystikos.gifting.adapter.web.dto;

import com.mystikos.gifting.domain.model.UnlockRuleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "新增/更新礼物请求")
public class SaveGiftRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "礼物编码")
    @NotBlank
    private String code;

    @Schema(description = "礼物展示名")
    @NotBlank
    private String name;

    @Schema(description = "图标标识/URL")
    private String icon;

    @Schema(description = "单价（星辉石）")
    @NotNull
    @Positive
    private BigDecimal price;

    @Schema(description = "所属档位ID")
    @NotNull
    private Long tierId;

    @Schema(description = "解锁规则类型，缺省为 NONE")
    private UnlockRuleType unlockRuleType;

    @Schema(description = "解锁规则阈值，非 NONE 类型时必填")
    private BigDecimal unlockRuleThreshold;

    @Schema(description = "是否上架，缺省为 true")
    private Boolean active;
}
