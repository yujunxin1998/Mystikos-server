package com.mystikos.relationship.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "更新亲密度模块配置请求")
public class UpdateRelationshipSettingsRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "每日亲密度获取上限")
    @NotNull
    @Positive
    private BigDecimal dailyIntimacyCap;
}
