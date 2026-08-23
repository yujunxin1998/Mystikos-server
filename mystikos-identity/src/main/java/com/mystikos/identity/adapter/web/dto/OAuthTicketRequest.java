package com.mystikos.identity.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OAuthTicketRequest {

    @NotBlank
    private String ticket;
}
