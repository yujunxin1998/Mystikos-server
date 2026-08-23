package com.mystikos.membership.adapter.web.dto;

import com.mystikos.membership.application.service.MembershipView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "会员成长视图")
public class MembershipResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "老板用户ID")
    private Long patronId;

    @Schema(description = "等级数值")
    private Integer tierLevel;

    @Schema(description = "等级编码")
    private String tierCode;

    @Schema(description = "等级展示名")
    private String tierDisplayName;

    @Schema(description = "累计消费金额")
    private BigDecimal cumulativeSpend;

    public static MembershipResponse from(MembershipView view) {
        MembershipResponse dto = new MembershipResponse();
        dto.setPatronId(view.patronId());
        dto.setTierLevel(view.tierLevel());
        dto.setTierCode(view.tierCode());
        dto.setTierDisplayName(view.tierDisplayName());
        dto.setCumulativeSpend(view.cumulativeSpend());
        return dto;
    }
}
