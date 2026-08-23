package com.mystikos.identity.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "陪玩名片审核请求")
public class ReviewCompanionShowcaseRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "是否通过，必填")
    @NotNull
    private Boolean approved;

    @Schema(description = "审核意见，审核不通过时必填")
    private String comment;
}
