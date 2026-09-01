package com.mystikos.gifting.adapter.web.dto;

import com.mystikos.gifting.domain.model.GiftTier;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "礼物档位视图")
public class GiftTierView implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "档位ID")
    private Long id;

    @Schema(description = "档位编码")
    private String code;

    @Schema(description = "展示名")
    private String displayName;

    @Schema(description = "英文展示名")
    private String displayNameEn;

    @Schema(description = "亲密度倍率")
    private BigDecimal multiplier;

    @Schema(description = "排序位次")
    private Integer sortOrder;

    @Schema(description = "是否启用")
    private Boolean active;

    public static GiftTierView from(GiftTier tier) {
        GiftTierView view = new GiftTierView();
        view.setId(tier.getId());
        view.setCode(tier.getCode());
        view.setDisplayName(tier.getDisplayName());
        view.setDisplayNameEn(tier.getDisplayNameEn());
        view.setMultiplier(tier.getMultiplier());
        view.setSortOrder(tier.getSortOrder());
        view.setActive(tier.isActive());
        return view;
    }
}
