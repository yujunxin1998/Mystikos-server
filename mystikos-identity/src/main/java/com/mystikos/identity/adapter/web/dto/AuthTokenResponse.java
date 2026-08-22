package com.mystikos.identity.adapter.web.dto;

import com.mystikos.identity.application.service.AuthResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "Token 响应")
public class AuthTokenResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Access Token（JWT，短期有效）")
    private String accessToken;

    @Schema(description = "Refresh Token（长期有效，可吊销）")
    private String refreshToken;

    @Schema(description = "用户ID")
    private Long userId;

    public static AuthTokenResponse from(AuthResult result) {
        AuthTokenResponse response = new AuthTokenResponse();
        response.setAccessToken(result.accessToken());
        response.setRefreshToken(result.refreshToken());
        response.setUserId(result.userId());
        return response;
    }
}
