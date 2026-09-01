package com.mystikos.commerce.adapter.web;

import com.mystikos.commerce.adapter.web.dto.AddressResponse;
import com.mystikos.commerce.adapter.web.dto.CreateAddressRequest;
import com.mystikos.commerce.application.command.CreateAddressCommand;
import com.mystikos.commerce.application.command.UpdateAddressCommand;
import com.mystikos.commerce.application.service.AddressApplicationService;
import com.mystikos.common.result.APIResponse;
import com.mystikos.common.security.CurrentUserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 老板收货地址簿。新增/编辑复用同一个请求体形状（{@link CreateAddressRequest}），
 * 没有单独定义一个字段完全一样的 UpdateAddressRequest。
 */
@RestController
@RequestMapping("/api/v1/addresses")
@Tag(name = "商城", description = "收货地址簿")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {

    private final AddressApplicationService addressApplicationService;

    public AddressController(AddressApplicationService addressApplicationService) {
        this.addressApplicationService = addressApplicationService;
    }

    @GetMapping
    @Operation(summary = "我的地址簿")
    public APIResponse<List<AddressResponse>> list() {
        return APIResponse.ok(addressApplicationService.listAddresses(currentPatronId()).stream()
                .map(AddressResponse::from)
                .toList());
    }

    @PostMapping
    @Operation(summary = "新增地址")
    public APIResponse<Long> create(@Valid @RequestBody CreateAddressRequest request) {
        Long addressId = addressApplicationService.createAddress(new CreateAddressCommand(
                currentPatronId(), request.getAddressType(), request.getRecipientName(), request.getPhone(),
                request.getCountryCode(), request.getProvinceCode(), request.getCity(), request.getDistrict(),
                request.getAddressLine1(), request.getAddressLine2(), request.getStateRegion(),
                request.getPostalCode(), request.isSetDefault()));
        return APIResponse.ok(addressId);
    }

    @PutMapping("/{addressId}")
    @Operation(summary = "编辑地址")
    public APIResponse<Void> update(@Parameter(description = "地址ID") @PathVariable Long addressId,
                                     @Valid @RequestBody CreateAddressRequest request) {
        addressApplicationService.updateAddress(addressId, currentPatronId(), new UpdateAddressCommand(
                request.getAddressType(), request.getRecipientName(), request.getPhone(), request.getCountryCode(),
                request.getProvinceCode(), request.getCity(), request.getDistrict(), request.getAddressLine1(),
                request.getAddressLine2(), request.getStateRegion(), request.getPostalCode(),
                request.isSetDefault()));
        return APIResponse.ok();
    }

    @DeleteMapping("/{addressId}")
    @Operation(summary = "删除地址")
    public APIResponse<Void> delete(@Parameter(description = "地址ID") @PathVariable Long addressId) {
        addressApplicationService.deleteAddress(addressId, currentPatronId());
        return APIResponse.ok();
    }

    @PostMapping("/{addressId}/default")
    @Operation(summary = "设为默认地址")
    public APIResponse<Void> setDefault(@Parameter(description = "地址ID") @PathVariable Long addressId) {
        addressApplicationService.setDefaultAddress(addressId, currentPatronId());
        return APIResponse.ok();
    }

    private Long currentPatronId() {
        return Long.valueOf(CurrentUserContext.get().userId());
    }
}
