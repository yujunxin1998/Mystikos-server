package com.mystikos.commerce.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "加入心愿单请求")
public class AddToWishlistRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID")
    @NotNull
    private Long productId;
}
