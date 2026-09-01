package com.mystikos.gifting.domain.repository;

import com.mystikos.gifting.domain.model.GiftCatalogItem;

import java.util.List;
import java.util.Optional;

public interface GiftCatalogRepository {

    List<GiftCatalogItem> findAllActive();

    /** 管理端用：含已下架的礼物。 */
    List<GiftCatalogItem> findAll();

    Optional<GiftCatalogItem> findById(Long id);

    /** id 为空则新增，否则整行覆盖更新——目录是运营配置数据，没有部分字段更新的需求。 */
    GiftCatalogItem save(GiftCatalogItem item);
}
