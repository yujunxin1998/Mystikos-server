package com.mystikos.identity.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OAuthUnbindRequest {

    @NotBlank
    @Pattern(regexp = "\\d{6}")
    private String verificationCode;
}
