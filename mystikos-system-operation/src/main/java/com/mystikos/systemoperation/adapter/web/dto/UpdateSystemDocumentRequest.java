package com.mystikos.systemoperation.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "新建或更新系统文档请求")
public class UpdateSystemDocumentRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "标题")
    @NotBlank
    private String title;

    @Schema(description = "正文内容")
    @NotBlank
    private String content;
}
