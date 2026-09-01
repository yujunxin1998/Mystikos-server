package com.mystikos.commerce.adapter.web.dto;

import com.mystikos.commerce.domain.model.AddressType;
import com.mystikos.commerce.domain.model.PatronAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "收货地址")
public class AddressResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "地址ID")
    private Long id;

    @Schema(description = "地址类型")
    private AddressType addressType;

    @Schema(description = "收件人姓名")
    private String recipientName;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "国家编码")
    private String countryCode;

    @Schema(description = "省份编码")
    private String provinceCode;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区/县")
    private String district;

    @Schema(description = "详细地址")
    private String addressLine1;

    @Schema(description = "详细地址补充")
    private String addressLine2;

    @Schema(description = "州/大区")
    private String stateRegion;

    @Schema(description = "邮政编码")
    private String postalCode;

    @Schema(description = "是否默认地址")
    private boolean isDefault;

    public static AddressResponse from(PatronAddress address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setAddressType(address.getAddressType());
        response.setRecipientName(address.getRecipientName());
        response.setPhone(address.getPhone());
        response.setCountryCode(address.getCountryCode());
        response.setProvinceCode(address.getProvinceCode());
        response.setCity(address.getCity());
        response.setDistrict(address.getDistrict());
        response.setAddressLine1(address.getAddressLine1());
        response.setAddressLine2(address.getAddressLine2());
        response.setStateRegion(address.getStateRegion());
        response.setPostalCode(address.getPostalCode());
        response.setDefault(address.isDefault());
        return response;
    }
}
