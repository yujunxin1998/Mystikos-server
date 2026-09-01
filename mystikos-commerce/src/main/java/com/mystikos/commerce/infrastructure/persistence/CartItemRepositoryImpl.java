package com.mystikos.commerce.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mystikos.commerce.domain.model.CartItem;
import com.mystikos.commerce.domain.repository.CartItemRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CartItemRepositoryImpl implements CartItemRepository {

    private final CartItemMapper mapper;

    public CartItemRepositoryImpl(CartItemMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<CartItem> findByPatronAndProduct(Long patronId, Long productId) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<CartItemPO>lambdaQuery()
                        .eq(CartItemPO::getPatronId, patronId)
                        .eq(CartItemPO::getProductId, productId)))
                .map(this::toDomain);
    }

    @Override
    public List<CartItem> findAllByPatron(Long patronId) {
        return mapper.selectList(Wrappers.<CartItemPO>lambdaQuery()
                        .eq(CartItemPO::getPatronId, patronId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public CartItem save(CartItem cartItem) {
        CartItemPO po = new CartItemPO();
        po.setId(cartItem.getId());
        po.setPatronId(cartItem.getPatronId());
        po.setProductId(cartItem.getProductId());
        po.setQuantity(cartItem.getQuantity());
        if (po.getId() == null) {
            mapper.insert(po);
            cartItem.assignId(po.getId());
        } else {
            mapper.updateById(po);
        }
        return cartItem;
    }

    @Override
    public void deleteByPatronAndProduct(Long patronId, Long productId) {
        mapper.delete(Wrappers.<CartItemPO>lambdaQuery()
                .eq(CartItemPO::getPatronId, patronId)
                .eq(CartItemPO::getProductId, productId));
    }

    @Override
    public void deleteAllByPatron(Long patronId) {
        mapper.delete(Wrappers.<CartItemPO>lambdaQuery().eq(CartItemPO::getPatronId, patronId));
    }

    @Override
    public void deleteByPatronAndProducts(Long patronId, List<Long> productIds) {
        mapper.delete(Wrappers.<CartItemPO>lambdaQuery()
                .eq(CartItemPO::getPatronId, patronId)
                .in(CartItemPO::getProductId, productIds));
    }

    private CartItem toDomain(CartItemPO po) {
        return CartItem.restore(po.getId(), po.getPatronId(), po.getProductId(), po.getQuantity());
    }
}
