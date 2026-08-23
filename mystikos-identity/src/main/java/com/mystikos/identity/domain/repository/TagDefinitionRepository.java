package com.mystikos.identity.domain.repository;

import com.mystikos.identity.domain.model.TagDefinition;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagDefinitionRepository {

    TagDefinition save(TagDefinition tag);

    Optional<TagDefinition> findById(Long id);

    List<TagDefinition> findByIds(Set<Long> ids);

    List<TagDefinition> findByCategory(String category, boolean onlyEnabled);
}
