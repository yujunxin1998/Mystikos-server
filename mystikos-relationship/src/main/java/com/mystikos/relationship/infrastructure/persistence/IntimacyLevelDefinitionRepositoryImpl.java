package com.mystikos.relationship.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mystikos.relationship.domain.model.IntimacyLevelDefinition;
import com.mystikos.relationship.domain.repository.IntimacyLevelDefinitionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class IntimacyLevelDefinitionRepositoryImpl implements IntimacyLevelDefinitionRepository {

    private final IntimacyLevelDefinitionMapper mapper;

    public IntimacyLevelDefinitionRepositoryImpl(IntimacyLevelDefinitionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<IntimacyLevelDefinition> findAll() {
        return mapper.selectList(Wrappers.<IntimacyLevelDefinitionPO>lambdaQuery()
                        .orderByAsc(IntimacyLevelDefinitionPO::getSortOrder))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<IntimacyLevelDefinition> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public IntimacyLevelDefinition save(IntimacyLevelDefinition definition) {
        IntimacyLevelDefinitionPO po = toPO(definition);
        if (po.getId() == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
        return toDomain(po);
    }

    private IntimacyLevelDefinitionPO toPO(IntimacyLevelDefinition definition) {
        IntimacyLevelDefinitionPO po = new IntimacyLevelDefinitionPO();
        po.setId(definition.getId());
        po.setCode(definition.getCode());
        po.setDisplayNameZh(definition.getDisplayName());
        po.setDisplayNameEn(definition.getDisplayNameEn());
        po.setThreshold(definition.getThreshold());
        po.setPerkDescription(definition.getPerkDescription());
        po.setSortOrder(definition.getSortOrder());
        return po;
    }

    private IntimacyLevelDefinition toDomain(IntimacyLevelDefinitionPO po) {
        return new IntimacyLevelDefinition(po.getId(), po.getCode(), po.getDisplayNameZh(), po.getDisplayNameEn(),
                po.getThreshold(), po.getPerkDescription(), po.getSortOrder());
    }
}
