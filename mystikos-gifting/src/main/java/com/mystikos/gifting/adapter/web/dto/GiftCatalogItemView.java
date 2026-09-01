package com.mystikos.gifting.adapter.web.dto;

import com.mystikos.gifting.domain.model.GiftCatalogItem;
import com.mystikos.gifting.domain.model.GiftTier;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "礼物目录条目视图")
public class GiftCatalogItemView implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "礼物ID")
    private Long id;

    @Schema(description = "礼物编码")
    private String code;

    @Schema(description = "礼物展示名")
    private String name;

    @Schema(description = "图标标识/URL")
    private String icon;

    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "所属档位ID")
    private Long tierId;

    @Schema(description = "所属档位编码")
    private String tierCode;

    @Schema(description = "所属档位展示名")
    private String tierDisplayName;

    @Schema(description = "档位倍率")
    private BigDecimal tierMultiplier;

    @Schema(description = "赠送 1 件预计获得的亲密度（= price x 倍率）")
    private BigDecimal intimacyPreview;

    @Schema(description = "解锁规则类型")
    private String unlockRuleType;

    @Schema(description = "解锁规则阈值，NONE 类型时为空")
    private BigDecimal unlockRuleThreshold;

    @Schema(description = "是否上架")
    private Boolean active;

    /** 目录列表接口用：礼物一定能找到自己的档位（tierId 有外键约束保证）。 */
    public static GiftCatalogItemView from(GiftCatalogItem item, GiftTier tier) {
        GiftCatalogItemView view = new GiftCatalogItemView();
        view.setId(item.getId());
        view.setCode(item.getCode());
        view.setName(item.getName());
        view.setIcon(item.getIcon());
        view.setPrice(item.getPrice());
        view.setTierId(item.getTierId());
        view.setUnlockRuleType(item.getUnlockRule().type().name());
        view.setUnlockRuleThreshold(item.getUnlockRule().threshold());
        view.setActive(item.isActive());
        if (tier != null) {
            view.setTierCode(tier.getCode());
            view.setTierDisplayName(tier.getDisplayName());
            view.setTierMultiplier(tier.getMultiplier());
            view.setIntimacyPreview(item.getPrice().multiply(tier.getMultiplier()));
        }
        return view;
    }
}
