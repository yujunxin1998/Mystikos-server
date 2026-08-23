package com.mystikos.relationship.adapter.web.dto;

import com.mystikos.relationship.application.service.IntimacyView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "亲密度视图")
public class IntimacyResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "老板用户ID")
    private Long patronId;

    @Schema(description = "陪玩用户ID")
    private Long companionId;

    @Schema(description = "亲密度阶段：0-4，尚无互动记录时为 0")
    private Integer stage;

    @Schema(description = "累计互动进度值")
    private BigDecimal progressValue;

    public static IntimacyResponse from(IntimacyView view) {
        IntimacyResponse dto = new IntimacyResponse();
        dto.setPatronId(view.patronId());
        dto.setCompanionId(view.companionId());
        dto.setStage(view.stage());
        dto.setProgressValue(view.progressValue());
        return dto;
    }
}
