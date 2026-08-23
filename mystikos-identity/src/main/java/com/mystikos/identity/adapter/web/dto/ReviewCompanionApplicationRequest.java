package com.mystikos.identity.adapter.web.dto;

import com.mystikos.identity.domain.model.AssessmentResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "录入陪玩身份申请考核结果请求")
public class ReviewCompanionApplicationRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "考核人用户ID，从系统用户里查询关联，必填")
    @NotNull
    private Long reviewerId;

    @Schema(description = "考核结果：PASS/FAIL，必填")
    @NotNull
    private AssessmentResult result;

    @Schema(description = "审核意见，可为空")
    private String comment;
}
