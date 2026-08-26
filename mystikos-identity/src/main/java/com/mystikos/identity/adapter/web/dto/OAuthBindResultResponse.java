package com.mystikos.identity.adapter.web.dto;

import com.mystikos.identity.application.service.OAuthBindOutcome;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "第三方账号绑定结果")
public class OAuthBindResultResponse {

    @Schema(description = "第三方平台，如 discord")
    private String provider;

    @Schema(description = "第三方平台上的用户 ID")
    private String providerUserId;

    @Schema(description = "第三方平台展示名")
    private String displayName;

    public static OAuthBindResultResponse from(OAuthBindOutcome outcome) {
        OAuthBindResultResponse response = new OAuthBindResultResponse();
        response.setProvider(outcome.provider());
        response.setProviderUserId(outcome.providerUserId());
        response.setDisplayName(outcome.displayName());
        return response;
    }
}
