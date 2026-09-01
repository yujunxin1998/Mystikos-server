package com.mystikos.gifting.domain.repository;

import com.mystikos.gifting.domain.model.GiftTier;

import java.util.List;
import java.util.Optional;

public interface GiftTierRepository {

    List<GiftTier> findAllActive();

    /** 管理端用：含已停用的档位。 */
    List<GiftTier> findAll();

    Optional<GiftTier> findById(Long id);

    /** id 为空则新增，否则整行覆盖更新。 */
    GiftTier save(GiftTier tier);
}
