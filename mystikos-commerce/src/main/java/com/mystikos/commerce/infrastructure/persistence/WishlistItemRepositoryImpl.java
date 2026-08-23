package com.mystikos.commerce.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mystikos.commerce.domain.model.WishlistItem;
import com.mystikos.commerce.domain.repository.WishlistItemRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class WishlistItemRepositoryImpl implements WishlistItemRepository {

    private final WishlistItemMapper mapper;

    public WishlistItemRepositoryImpl(WishlistItemMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<WishlistItem> findByPatronAndProduct(Long patronId, Long productId) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<WishlistItemPO>lambdaQuery()
                        .eq(WishlistItemPO::getPatronId, patronId)
                        .eq(WishlistItemPO::getProductId, productId)))
                .map(this::toDomain);
    }

    @Override
    public List<WishlistItem> findAllByPatron(Long patronId) {
        return mapper.selectList(Wrappers.<WishlistItemPO>lambdaQuery()
                        .eq(WishlistItemPO::getPatronId, patronId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public WishlistItem save(WishlistItem wishlistItem) {
        WishlistItemPO po = new WishlistItemPO();
        po.setId(wishlistItem.getId());
        po.setPatronId(wishlistItem.getPatronId());
        po.setProductId(wishlistItem.getProductId());
        po.setAddedAt(wishlistItem.getAddedAt());
        mapper.insert(po);
        wishlistItem.assignId(po.getId());
        return wishlistItem;
    }

    @Override
    public void deleteByPatronAndProduct(Long patronId, Long productId) {
        mapper.delete(Wrappers.<WishlistItemPO>lambdaQuery()
                .eq(WishlistItemPO::getPatronId, patronId)
                .eq(WishlistItemPO::getProductId, productId));
    }

    private WishlistItem toDomain(WishlistItemPO po) {
        return WishlistItem.restore(po.getId(), po.getPatronId(), po.getProductId(), po.getAddedAt());
    }
}
