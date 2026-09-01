package com.mystikos.commerce.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mystikos.commerce.domain.model.AddressType;
import com.mystikos.commerce.domain.model.PatronAddress;
import com.mystikos.commerce.domain.repository.PatronAddressRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PatronAddressRepositoryImpl implements PatronAddressRepository {

    private final PatronAddressMapper mapper;

    public PatronAddressRepositoryImpl(PatronAddressMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<PatronAddress> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<PatronAddress> findAllByPatron(Long patronId) {
        return mapper.selectList(Wrappers.<PatronAddressPO>lambdaQuery()
                        .eq(PatronAddressPO::getPatronId, patronId)
                        .orderByDesc(PatronAddressPO::getIsDefault)
                        .orderByDesc(PatronAddressPO::getUpdatedAt))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PatronAddress save(PatronAddress address) {
        OffsetDateTime now = OffsetDateTime.now();
        PatronAddressPO po = new PatronAddressPO();
        po.setId(address.getId());
        po.setPatronId(address.getPatronId());
        po.setAddressType(address.getAddressType().name());
        po.setRecipientName(address.getRecipientName());
        po.setPhone(address.getPhone());
        po.setCountryCode(address.getCountryCode());
        po.setProvinceCode(address.getProvinceCode());
        po.setCity(address.getCity());
        po.setDistrict(address.getDistrict());
        po.setAddressLine1(address.getAddressLine1());
        po.setAddressLine2(address.getAddressLine2());
        po.setStateRegion(address.getStateRegion());
        po.setPostalCode(address.getPostalCode());
        po.setIsDefault(address.isDefault());
        po.setUpdatedAt(now);
        if (po.getId() == null) {
            po.setCreatedAt(now);
            mapper.insert(po);
            address.assignId(po.getId());
        } else {
            mapper.updateById(po);
        }
        return address;
    }

    @Override
    public void deleteById(Long id, Long patronId) {
        mapper.delete(Wrappers.<PatronAddressPO>lambdaQuery()
                .eq(PatronAddressPO::getId, id)
                .eq(PatronAddressPO::getPatronId, patronId));
    }

    @Override
    public void clearDefault(Long patronId) {
        mapper.update(null, Wrappers.<PatronAddressPO>lambdaUpdate()
                .eq(PatronAddressPO::getPatronId, patronId)
                .eq(PatronAddressPO::getIsDefault, true)
                .set(PatronAddressPO::getIsDefault, false)
                .set(PatronAddressPO::getUpdatedAt, OffsetDateTime.now()));
    }

    private PatronAddress toDomain(PatronAddressPO po) {
        return PatronAddress.restore(po.getId(), po.getPatronId(), AddressType.valueOf(po.getAddressType()),
                po.getRecipientName(), po.getPhone(), po.getCountryCode(), po.getProvinceCode(), po.getCity(),
                po.getDistrict(), po.getAddressLine1(), po.getAddressLine2(), po.getStateRegion(),
                po.getPostalCode(), Boolean.TRUE.equals(po.getIsDefault()));
    }
}
