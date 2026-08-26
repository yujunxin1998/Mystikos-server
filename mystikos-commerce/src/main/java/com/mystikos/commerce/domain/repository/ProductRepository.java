package com.mystikos.commerce.domain.repository;

import com.mystikos.commerce.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(Long id);

    List<Product> findAllOnShelf();
}
