package com.mystikos.identity.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "第三方账号绑定授权 URL")
public class OAuthAuthorizeUrlResponse {

    @Schema(description = "前端需要跳转到的 Discord 授权地址")
    private String authorizeUrl;

    public OAuthAuthorizeUrlResponse() {
    }

    public OAuthAuthorizeUrlResponse(String authorizeUrl) {
        this.authorizeUrl = authorizeUrl;
    }
}
