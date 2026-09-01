package com.mystikos.relationship.adapter.web.dto;

import com.mystikos.relationship.domain.model.RelationshipSettings;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "亲密度模块配置视图")
public class RelationshipSettingsResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "每日亲密度获取上限")
    private BigDecimal dailyIntimacyCap;

    public static RelationshipSettingsResponse from(RelationshipSettings settings) {
        RelationshipSettingsResponse dto = new RelationshipSettingsResponse();
        dto.setDailyIntimacyCap(settings.getDailyIntimacyCap());
        return dto;
    }
}
