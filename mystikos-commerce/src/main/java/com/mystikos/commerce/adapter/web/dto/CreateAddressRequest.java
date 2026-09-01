package com.mystikos.commerce.adapter.web.dto;

import com.mystikos.commerce.domain.model.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "新增/编辑收货地址请求")
public class CreateAddressRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Schema(description = "地址类型：DOMESTIC=国内 / OVERSEAS=海外")
    private AddressType addressType;

    @NotBlank
    @Schema(description = "收件人姓名")
    private String recipientName;

    @NotBlank
    @Schema(description = "联系电话")
    private String phone;

    @NotBlank
    @Schema(description = "国家编码，引用行政区划表；国内地址固定为 CN")
    private String countryCode;

    @Schema(description = "省份编码，引用行政区划表；仅国内地址必填")
    private String provinceCode;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区/县；仅国内地址必填")
    private String district;

    @NotBlank
    @Schema(description = "详细地址")
    private String addressLine1;

    @Schema(description = "详细地址补充")
    private String addressLine2;

    @Schema(description = "州/大区；仅海外地址可选")
    private String stateRegion;

    @Schema(description = "邮政编码")
    private String postalCode;

    @Schema(description = "是否设为默认地址")
    private boolean setDefault;
}
