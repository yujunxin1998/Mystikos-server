package com.mystikos.identity.adapter.web.dto;

import com.mystikos.identity.application.service.TagView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "标签视图")
public class TagResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "标签ID")
    private Long id;

    @Schema(description = "标签分类，如 GAME_TYPE")
    private String category;

    @Schema(description = "标签展示名")
    private String label;

    @Schema(description = "排序权重")
    private int sortOrder;

    @Schema(description = "是否启用")
    private boolean enabled;

    public static TagResponse from(TagView view) {
        TagResponse response = new TagResponse();
        response.setId(view.id());
        response.setCategory(view.category());
        response.setLabel(view.label());
        response.setSortOrder(view.sortOrder());
        response.setEnabled(view.enabled());
        return response;
    }
}
