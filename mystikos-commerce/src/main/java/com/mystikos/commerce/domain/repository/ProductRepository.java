package com.mystikos.commerce.domain.repository;

import com.mystikos.commerce.domain.model.Product;
import com.mystikos.commerce.domain.model.ProductStatus;
import com.mystikos.common.result.PageResult;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(Long id);

    List<Product> findAllOnShelf();

    /** 后台分页查询商品，status 为空则不限状态（含未上架/已下架）。 */
    PageResult<Product> findPage(ProductStatus status, int pageNum, int pageSize);
}
