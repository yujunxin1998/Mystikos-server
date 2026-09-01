package com.mystikos.membership.domain.repository;

import com.mystikos.membership.domain.model.MembershipTierDefinition;

import java.util.List;
import java.util.Optional;

public interface MembershipTierDefinitionRepository {

    /** 全部等级，按 sortOrder 升序——LevelResolver 需要的就是这个顺序。 */
    List<MembershipTierDefinition> findAll();

    Optional<MembershipTierDefinition> findByCode(String code);

    /** id 为空则新增，否则整行覆盖更新。 */
    MembershipTierDefinition save(MembershipTierDefinition definition);
}
