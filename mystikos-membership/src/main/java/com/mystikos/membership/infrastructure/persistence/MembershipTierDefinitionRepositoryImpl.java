package com.mystikos.membership.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mystikos.membership.domain.model.MembershipTierDefinition;
import com.mystikos.membership.domain.repository.MembershipTierDefinitionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MembershipTierDefinitionRepositoryImpl implements MembershipTierDefinitionRepository {

    private final MembershipTierDefinitionMapper mapper;

    public MembershipTierDefinitionRepositoryImpl(MembershipTierDefinitionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<MembershipTierDefinition> findAll() {
        return mapper.selectList(Wrappers.<MembershipTierDefinitionPO>lambdaQuery()
                        .orderByAsc(MembershipTierDefinitionPO::getSortOrder))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<MembershipTierDefinition> findByCode(String code) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<MembershipTierDefinitionPO>lambdaQuery()
                        .eq(MembershipTierDefinitionPO::getCode, code)))
                .map(this::toDomain);
    }

    @Override
    public MembershipTierDefinition save(MembershipTierDefinition definition) {
        MembershipTierDefinitionPO po = toPO(definition);
        if (po.getId() == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
        return toDomain(po);
    }

    private MembershipTierDefinitionPO toPO(MembershipTierDefinition definition) {
        MembershipTierDefinitionPO po = new MembershipTierDefinitionPO();
        po.setId(definition.getId());
        po.setCode(definition.getCode());
        po.setDisplayName(definition.getDisplayName());
        po.setDisplayNameEn(definition.getDisplayNameEn());
        po.setLevel(definition.getLevel());
        po.setCumulativeSpendThreshold(definition.getThreshold());
        po.setPerkDescription(definition.getPerkDescription());
        po.setSortOrder(definition.getSortOrder());
        return po;
    }

    private MembershipTierDefinition toDomain(MembershipTierDefinitionPO po) {
        return new MembershipTierDefinition(po.getId(), po.getCode(), po.getDisplayName(), po.getDisplayNameEn(),
                po.getLevel(), po.getCumulativeSpendThreshold(), po.getPerkDescription(), po.getSortOrder());
    }
}
