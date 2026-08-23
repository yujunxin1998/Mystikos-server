package com.mystikos.identity.domain.repository;

import com.mystikos.identity.domain.model.CompanionShowcase;

import java.util.Optional;

public interface CompanionShowcaseRepository {

    CompanionShowcase save(CompanionShowcase showcase);

    Optional<CompanionShowcase> findByUserId(Long userId);
}
