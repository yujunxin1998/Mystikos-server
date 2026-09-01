package com.mystikos.gifting.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mystikos.gifting.domain.model.GiftTier;
import com.mystikos.gifting.domain.repository.GiftTierRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class GiftTierRepositoryImpl implements GiftTierRepository {

    private final GiftTierMapper mapper;

    public GiftTierRepositoryImpl(GiftTierMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<GiftTier> findAllActive() {
        return mapper.selectList(Wrappers.<GiftTierPO>lambdaQuery()
                        .eq(GiftTierPO::getActive, true)
                        .orderByAsc(GiftTierPO::getSortOrder))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<GiftTier> findAll() {
        return mapper.selectList(Wrappers.<GiftTierPO>lambdaQuery()
                        .orderByAsc(GiftTierPO::getSortOrder))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<GiftTier> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public GiftTier save(GiftTier tier) {
        GiftTierPO po = toPO(tier);
        if (po.getId() == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
        return toDomain(po);
    }

    private GiftTierPO toPO(GiftTier tier) {
        GiftTierPO po = new GiftTierPO();
        po.setId(tier.getId());
        po.setCode(tier.getCode());
        po.setDisplayName(tier.getDisplayName());
        po.setDisplayNameEn(tier.getDisplayNameEn());
        po.setMultiplier(tier.getMultiplier());
        po.setSortOrder(tier.getSortOrder());
        po.setActive(tier.isActive());
        return po;
    }

    private GiftTier toDomain(GiftTierPO po) {
        return new GiftTier(po.getId(), po.getCode(), po.getDisplayName(), po.getDisplayNameEn(),
                po.getMultiplier(), po.getSortOrder(), Boolean.TRUE.equals(po.getActive()));
    }
}
