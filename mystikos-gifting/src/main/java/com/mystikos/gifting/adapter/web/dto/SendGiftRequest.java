package com.mystikos.gifting.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "赠礼请求")
public class SendGiftRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "接收方（陪玩）用户ID")
    @NotNull
    private Long companionId;

    @Schema(description = "礼物ID")
    @NotNull
    private Long giftId;

    @Schema(description = "赠送数量")
    @NotNull
    @Min(1)
    private Integer quantity;
}
