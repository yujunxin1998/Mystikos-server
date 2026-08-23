package com.mystikos.gifting.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mystikos.gifting.domain.model.GiftCatalogItem;
import com.mystikos.gifting.domain.model.UnlockRule;
import com.mystikos.gifting.domain.model.UnlockRuleType;
import com.mystikos.gifting.domain.repository.GiftCatalogRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class GiftCatalogRepositoryImpl implements GiftCatalogRepository {

    private final GiftCatalogItemMapper mapper;

    public GiftCatalogRepositoryImpl(GiftCatalogItemMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<GiftCatalogItem> findAllActive() {
        return mapper.selectList(Wrappers.<GiftCatalogItemPO>lambdaQuery()
                        .eq(GiftCatalogItemPO::getActive, true))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<GiftCatalogItem> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    private GiftCatalogItem toDomain(GiftCatalogItemPO po) {
        UnlockRuleType type = UnlockRuleType.valueOf(po.getUnlockRuleType());
        UnlockRule rule = type == UnlockRuleType.NONE
                ? UnlockRule.none()
                : new UnlockRule(type, po.getUnlockRuleThreshold());
        return new GiftCatalogItem(po.getId(), po.getCode(), po.getName(), po.getIcon(),
                po.getPrice(), rule, Boolean.TRUE.equals(po.getActive()));
    }
}
