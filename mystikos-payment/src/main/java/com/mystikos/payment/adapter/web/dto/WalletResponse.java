package com.mystikos.payment.adapter.web.dto;

import com.mystikos.payment.domain.model.Wallet;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "钱包余额视图")
public class WalletResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "余额")
    private BigDecimal balance;

    @Schema(description = "结算币种")
    private String currency;

    public static WalletResponse from(Wallet wallet) {
        WalletResponse dto = new WalletResponse();
        dto.setBalance(wallet.getBalance());
        dto.setCurrency(wallet.getCurrency());
        return dto;
    }
}
