package com.mystikos.identity.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "更新隐私设置请求")
public class UpdatePrivacyRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "是否匿名上榜")
    @NotNull
    private Boolean anonymous;

    public boolean isAnonymous() {
        return Boolean.TRUE.equals(anonymous);
    }
}
