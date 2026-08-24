package com.mystikos.identity.adapter.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReorderCompanionShowcaseMediaRequest {
    @NotNull private List<String> photoObjectKeys;
    @NotNull private List<String> videoObjectKeys;
    @NotNull private List<String> audioObjectKeys;
}
