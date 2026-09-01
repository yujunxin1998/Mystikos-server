package com.mystikos.membership.adapter.web.dto;

import com.mystikos.membership.domain.model.MembershipTierDefinition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "VIP 等级视图")
public class MembershipTierView implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "等级ID")
    private Long id;

    @Schema(description = "等级编码")
    private String code;

    @Schema(description = "展示名")
    private String displayName;

    @Schema(description = "英文展示名")
    private String displayNameEn;

    @Schema(description = "对外展示的等级数值")
    private Integer level;

    @Schema(description = "进入该等级所需的最低累计消费")
    private BigDecimal cumulativeSpendThreshold;

    @Schema(description = "权益文案")
    private String perkDescription;

    @Schema(description = "排序位次")
    private Integer sortOrder;

    public static MembershipTierView from(MembershipTierDefinition definition) {
        MembershipTierView view = new MembershipTierView();
        view.setId(definition.getId());
        view.setCode(definition.getCode());
        view.setDisplayName(definition.getDisplayName());
        view.setDisplayNameEn(definition.getDisplayNameEn());
        view.setLevel(definition.getLevel());
        view.setCumulativeSpendThreshold(definition.getThreshold());
        view.setPerkDescription(definition.getPerkDescription());
        view.setSortOrder(definition.getSortOrder());
        return view;
    }
}
