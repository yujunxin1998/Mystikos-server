package com.mystikos.identity.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "更新昵称请求")
public class UpdateProfileRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "昵称")
    private String nickname;
}
