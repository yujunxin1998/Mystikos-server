package com.mystikos.identity.infrastructure.persistence;

import com.mystikos.identity.domain.model.CompanionShowcase;
import com.mystikos.identity.domain.repository.CompanionShowcaseRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class CompanionShowcaseRepositoryImpl implements CompanionShowcaseRepository {

    private final CompanionShowcaseMapper mapper;

    public CompanionShowcaseRepositoryImpl(CompanionShowcaseMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public CompanionShowcase save(CompanionShowcase showcase) {
        CompanionShowcasePO po = toPO(showcase);
        if (mapper.selectById(po.getUserId()) == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
        return showcase;
    }

    @Override
    public Optional<CompanionShowcase> findByUserId(Long userId) {
        CompanionShowcasePO po = mapper.selectById(userId);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    private CompanionShowcasePO toPO(CompanionShowcase showcase) {
        CompanionShowcasePO po = new CompanionShowcasePO();
        po.setUserId(showcase.getUserId());
        po.setPublishedRevisionId(showcase.getPublishedRevisionId());
        po.setPublishedAt(showcase.getPublishedAt());
        return po;
    }

    private CompanionShowcase toDomain(CompanionShowcasePO po) {
        return CompanionShowcase.restore(po.getUserId(), po.getPublishedRevisionId(), po.getPublishedAt());
    }
}
