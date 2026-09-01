package com.mystikos.relationship.domain.repository;

import com.mystikos.relationship.domain.model.RelationshipSettings;

public interface RelationshipSettingsRepository {

    /** 单行配置，取不到时返回 {@link RelationshipSettings#defaults()}，不抛异常。 */
    RelationshipSettings get();

    void save(RelationshipSettings settings);
}
