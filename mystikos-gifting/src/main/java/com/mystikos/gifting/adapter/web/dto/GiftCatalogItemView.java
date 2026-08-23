package com.mystikos.gifting.adapter.web.dto;

import com.mystikos.gifting.domain.model.GiftCatalogItem;
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

    @Schema(description = "解锁规则类型")
    private String unlockRuleType;

    @Schema(description = "解锁规则阈值，NONE 类型时为空")
    private BigDecimal unlockRuleThreshold;

    public static GiftCatalogItemView from(GiftCatalogItem item) {
        GiftCatalogItemView view = new GiftCatalogItemView();
        view.setId(item.getId());
        view.setCode(item.getCode());
        view.setName(item.getName());
        view.setIcon(item.getIcon());
        view.setPrice(item.getPrice());
        view.setUnlockRuleType(item.getUnlockRule().type().name());
        view.setUnlockRuleThreshold(item.getUnlockRule().threshold());
        return view;
    }
}
