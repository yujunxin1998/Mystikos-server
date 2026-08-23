package com.mystikos.identity.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

@Data
@Schema(description = "更新我的标签请求")
public class UpdateUserTagsRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "选中的标签ID集合，整体覆盖式更新，传空集合等于清空")
    private Set<Long> tagIds;
}
