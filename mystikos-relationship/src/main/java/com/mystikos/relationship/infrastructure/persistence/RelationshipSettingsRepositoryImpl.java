package com.mystikos.relationship.infrastructure.persistence;

import com.mystikos.relationship.domain.model.RelationshipSettings;
import com.mystikos.relationship.domain.repository.RelationshipSettingsRepository;
import org.springframework.stereotype.Repository;

@Repository
public class RelationshipSettingsRepositoryImpl implements RelationshipSettingsRepository {

    private static final Long SINGLETON_ID = 1L;

    private final RelationshipSettingsMapper mapper;

    public RelationshipSettingsRepositoryImpl(RelationshipSettingsMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public RelationshipSettings get() {
        RelationshipSettingsPO po = mapper.selectById(SINGLETON_ID);
        if (po == null) {
            return RelationshipSettings.defaults();
        }
        return new RelationshipSettings(po.getDailyIntimacyCap());
    }

    @Override
    public void save(RelationshipSettings settings) {
        RelationshipSettingsPO po = new RelationshipSettingsPO();
        po.setId(SINGLETON_ID);
        po.setDailyIntimacyCap(settings.getDailyIntimacyCap());
        if (mapper.selectById(SINGLETON_ID) == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
    }
}
