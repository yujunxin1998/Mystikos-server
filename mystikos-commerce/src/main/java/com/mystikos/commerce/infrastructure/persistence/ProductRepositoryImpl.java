package com.mystikos.commerce.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mystikos.commerce.domain.model.Product;
import com.mystikos.commerce.domain.model.ProductStatus;
import com.mystikos.commerce.domain.repository.ProductRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductMapper mapper;

    public ProductRepositoryImpl(ProductMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public List<Product> findAllOnShelf() {
        return mapper.selectList(Wrappers.<ProductPO>lambdaQuery()
                        .eq(ProductPO::getStatus, ProductStatus.ON_SHELF.name()))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Product toDomain(ProductPO po) {
        List<String> images = po.getImages() == null || po.getImages().isBlank()
                ? List.of()
                : Arrays.asList(po.getImages().split(","));
        return new Product(po.getId(), po.getCategoryId(), po.getName(), po.getDescription(),
                po.getPrice(), images, ProductStatus.valueOf(po.getStatus()));
    }
}
