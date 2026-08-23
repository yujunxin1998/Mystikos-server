package com.mystikos.identity.adapter.web.dto;

import com.mystikos.identity.domain.model.AuthChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyContactRequest {
    @NotNull
    private AuthChannel channel;

    @NotBlank
    private String identifier;

    @NotBlank
    @Pattern(regexp = "\\d{6}")
    private String verificationCode;
}
