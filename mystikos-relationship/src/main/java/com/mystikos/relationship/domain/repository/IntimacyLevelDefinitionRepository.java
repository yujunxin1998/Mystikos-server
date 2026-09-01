package com.mystikos.relationship.domain.repository;

import com.mystikos.relationship.domain.model.IntimacyLevelDefinition;

import java.util.List;
import java.util.Optional;

public interface IntimacyLevelDefinitionRepository {

    /** 全部等级，按 sortOrder 升序——LevelResolver 需要的就是这个顺序。 */
    List<IntimacyLevelDefinition> findAll();

    Optional<IntimacyLevelDefinition> findById(Long id);

    /** id 为空则新增，否则整行覆盖更新。 */
    IntimacyLevelDefinition save(IntimacyLevelDefinition definition);
}
