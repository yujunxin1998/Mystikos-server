package com.mystikos.commerce.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mystikos.commerce.domain.model.Product;
import com.mystikos.commerce.domain.model.ProductStatus;
import com.mystikos.commerce.domain.repository.ProductRepository;
import com.mystikos.common.result.PageResult;
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
    public Product save(Product product) {
        ProductPO po = toPO(product);
        if (po.getId() == null) {
            mapper.insert(po);
            product.assignId(po.getId());
        } else {
            mapper.updateById(po);
        }
        return product;
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

    @Override
    public PageResult<Product> findPage(ProductStatus status, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ProductPO> pos = mapper.selectList(Wrappers.<ProductPO>lambdaQuery()
                .eq(status != null, ProductPO::getStatus, status == null ? null : status.name())
                .orderByDesc(ProductPO::getId));
        PageInfo<ProductPO> pageInfo = new PageInfo<>(pos);
        List<Product> products = pos.stream().map(this::toDomain).toList();
        return PageResult.of(products, pageInfo.getTotal(), pageNum, pageSize);
    }

    private ProductPO toPO(Product product) {
        ProductPO po = new ProductPO();
        po.setId(product.getId());
        po.setCategoryId(product.getCategoryId());
        po.setName(product.getName());
        po.setDescription(product.getDescription());
        po.setPrice(product.getPrice());
        po.setImages(product.getImages() == null || product.getImages().isEmpty()
                ? null : String.join(",", product.getImages()));
        po.setStatus(product.getStatus().name());
        return po;
    }

    private Product toDomain(ProductPO po) {
        List<String> images = po.getImages() == null || po.getImages().isBlank()
                ? List.of()
                : Arrays.asList(po.getImages().split(","));
        return Product.restore(po.getId(), po.getCategoryId(), po.getName(), po.getDescription(),
                po.getPrice(), images, ProductStatus.valueOf(po.getStatus()));
    }
}
