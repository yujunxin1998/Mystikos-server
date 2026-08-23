package com.mystikos.identity.adapter.web.dto;

import com.mystikos.identity.domain.model.AuthChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContactVerificationCodeRequest {
    @NotNull
    private AuthChannel channel;

    @NotBlank
    private String identifier;
}
