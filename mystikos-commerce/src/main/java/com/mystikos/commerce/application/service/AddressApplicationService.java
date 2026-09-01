package com.mystikos.commerce.application.service;

import com.mystikos.commerce.application.command.CreateAddressCommand;
import com.mystikos.commerce.application.command.UpdateAddressCommand;
import com.mystikos.commerce.domain.CommerceException;
import com.mystikos.commerce.domain.model.PatronAddress;
import com.mystikos.commerce.domain.repository.PatronAddressRepository;
import com.mystikos.common.region.RegionQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 老板收货地址簿。跟 {@link CommerceApplicationService} 完全没有耦合（不依赖购物车/订单/商品），
 * 单独成一个 service——地址簿本身是自足的 CRUD，混进商城主服务只会让那个类更臃肿。
 */
@Service
public class AddressApplicationService {

    private final PatronAddressRepository patronAddressRepository;
    private final RegionQueryService regionQueryService;

    public AddressApplicationService(PatronAddressRepository patronAddressRepository,
                                      RegionQueryService regionQueryService) {
        this.patronAddressRepository = patronAddressRepository;
        this.regionQueryService = regionQueryService;
    }

    public List<PatronAddress> listAddresses(Long patronId) {
        return patronAddressRepository.findAllByPatron(patronId);
    }

    @Transactional
    public Long createAddress(CreateAddressCommand command) {
        validateRegionCodes(command.countryCode(), command.provinceCode());
        PatronAddress address = PatronAddress.create(command.patronId(), command.addressType(),
                command.recipientName(), command.phone(), command.countryCode(), command.provinceCode(),
                command.city(), command.district(), command.addressLine1(), command.addressLine2(),
                command.stateRegion(), command.postalCode(), command.setDefault());
        if (command.setDefault()) {
            patronAddressRepository.clearDefault(command.patronId());
        }
        return patronAddressRepository.save(address).getId();
    }

    @Transactional
    public void updateAddress(Long addressId, Long patronId, UpdateAddressCommand command) {
        validateRegionCodes(command.countryCode(), command.provinceCode());
        PatronAddress address = requireOwned(addressId, patronId);
        address.update(command.addressType(), command.recipientName(), command.phone(), command.countryCode(),
                command.provinceCode(), command.city(), command.district(), command.addressLine1(),
                command.addressLine2(), command.stateRegion(), command.postalCode());
        if (command.setDefault()) {
            patronAddressRepository.clearDefault(patronId);
            address.markDefault();
        }
        patronAddressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(Long addressId, Long patronId) {
        requireOwned(addressId, patronId);
        patronAddressRepository.deleteById(addressId, patronId);
    }

    @Transactional
    public void setDefaultAddress(Long addressId, Long patronId) {
        PatronAddress address = requireOwned(addressId, patronId);
        patronAddressRepository.clearDefault(patronId);
        address.markDefault();
        patronAddressRepository.save(address);
    }

    /** 供商城下单流程解析地址用，不做归属校验以外的事——归属校验交给调用方（下单时会再核对 patronId）。 */
    public PatronAddress requireOwned(Long addressId, Long patronId) {
        PatronAddress address = patronAddressRepository.findById(addressId)
                .orElseThrow(() -> CommerceException.addressNotFound(addressId));
        if (!address.getPatronId().equals(patronId)) {
            throw CommerceException.addressNotFound(addressId);
        }
        return address;
    }

    private void validateRegionCodes(String countryCode, String provinceCode) {
        if (countryCode != null && !regionQueryService.exists(countryCode)) {
            throw CommerceException.regionCodeInvalid(countryCode);
        }
        if (provinceCode != null && !regionQueryService.exists(provinceCode)) {
            throw CommerceException.regionCodeInvalid(provinceCode);
        }
    }
}
