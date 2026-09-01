package com.mystikos.relationship.adapter.web.dto;

import com.mystikos.relationship.domain.model.IntimacyLevelDefinition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "亲密度等级视图")
public class IntimacyLevelView implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "等级ID")
    private Long id;

    @Schema(description = "等级编码")
    private String code;

    @Schema(description = "中文展示名")
    private String displayNameZh;

    @Schema(description = "英文展示名")
    private String displayNameEn;

    @Schema(description = "进入该等级所需的最低累计进度")
    private BigDecimal threshold;

    @Schema(description = "权益文案")
    private String perkDescription;

    @Schema(description = "排序位次")
    private Integer sortOrder;

    public static IntimacyLevelView from(IntimacyLevelDefinition definition) {
        IntimacyLevelView view = new IntimacyLevelView();
        view.setId(definition.getId());
        view.setCode(definition.getCode());
        view.setDisplayNameZh(definition.getDisplayName());
        view.setDisplayNameEn(definition.getDisplayNameEn());
        view.setThreshold(definition.getThreshold());
        view.setPerkDescription(definition.getPerkDescription());
        view.setSortOrder(definition.getSortOrder());
        return view;
    }
}
