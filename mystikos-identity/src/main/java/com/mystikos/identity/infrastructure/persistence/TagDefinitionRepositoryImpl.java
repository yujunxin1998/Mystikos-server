package com.mystikos.identity.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mystikos.identity.domain.model.TagDefinition;
import com.mystikos.identity.domain.repository.TagDefinitionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class TagDefinitionRepositoryImpl implements TagDefinitionRepository {

    private final TagDefinitionMapper tagDefinitionMapper;

    public TagDefinitionRepositoryImpl(TagDefinitionMapper tagDefinitionMapper) {
        this.tagDefinitionMapper = tagDefinitionMapper;
    }

    @Override
    public TagDefinition save(TagDefinition tag) {
        TagDefinitionPO po = toPO(tag);
        if (po.getId() == null) {
            tagDefinitionMapper.insert(po);
            tag.assignId(po.getId());
        } else {
            tagDefinitionMapper.updateById(po);
        }
        return tag;
    }

    @Override
    public Optional<TagDefinition> findById(Long id) {
        return Optional.ofNullable(tagDefinitionMapper.selectById(id)).map(TagDefinitionRepositoryImpl::toDomain);
    }

    @Override
    public List<TagDefinition> findByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return tagDefinitionMapper.selectBatchIds(ids).stream()
                .map(TagDefinitionRepositoryImpl::toDomain)
                .toList();
    }

    @Override
    public List<TagDefinition> findByCategory(String category, boolean onlyEnabled) {
        LambdaQueryWrapper<TagDefinitionPO> wrapper = new LambdaQueryWrapper<TagDefinitionPO>()
                .eq(TagDefinitionPO::getCategory, category)
                .orderByAsc(TagDefinitionPO::getSortOrder);
        if (onlyEnabled) {
            wrapper.eq(TagDefinitionPO::getEnabled, true);
        }
        return tagDefinitionMapper.selectList(wrapper).stream()
                .map(TagDefinitionRepositoryImpl::toDomain)
                .toList();
    }

    private static TagDefinition toDomain(TagDefinitionPO po) {
        return TagDefinition.restore(po.getId(), po.getCategory(), po.getLabel(), po.getSortOrder(),
                Boolean.TRUE.equals(po.getEnabled()));
    }

    private static TagDefinitionPO toPO(TagDefinition tag) {
        TagDefinitionPO po = new TagDefinitionPO();
        po.setId(tag.getId());
        po.setCategory(tag.getCategory());
        po.setLabel(tag.getLabel());
        po.setSortOrder(tag.getSortOrder());
        po.setEnabled(tag.isEnabled());
        return po;
    }
}
