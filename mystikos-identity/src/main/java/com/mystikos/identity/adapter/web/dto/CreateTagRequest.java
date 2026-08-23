package com.mystikos.identity.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "新建标签请求")
public class CreateTagRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "标签分类，如 GAME_TYPE，自由字符串，不预置枚举")
    @NotBlank
    private String category;

    @Schema(description = "标签展示名")
    @NotBlank
    private String label;

    @Schema(description = "排序权重，越小越靠前，默认0")
    private int sortOrder;
}
