package com.mystikos.gifting.domain.repository;

import com.mystikos.gifting.domain.model.GiftCatalogItem;

import java.util.List;
import java.util.Optional;

public interface GiftCatalogRepository {

    List<GiftCatalogItem> findAllActive();

    Optional<GiftCatalogItem> findById(Long id);
}
