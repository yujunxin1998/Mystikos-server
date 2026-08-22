package com.mystikos.common.storage.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Schema(description = "上传结果")
@AllArgsConstructor
@NoArgsConstructor
public class UploadResult {
    @Schema(description = "对象键，后续查询/删除都用这个")
    public String objectKey;
    @Schema(description = "预签名下载链接（限时有效）")
    public String url;
}
