package com.mystikos.relationship.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mystikos.relationship.domain.model.IntimacyRecord;
import com.mystikos.relationship.domain.repository.IntimacyRecordRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class IntimacyRecordRepositoryImpl implements IntimacyRecordRepository {

    private final IntimacyRecordMapper mapper;

    public IntimacyRecordRepositoryImpl(IntimacyRecordMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<IntimacyRecord> findByPatronAndCompanion(Long patronId, Long companionId) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<IntimacyRecordPO>lambdaQuery()
                        .eq(IntimacyRecordPO::getPatronId, patronId)
                        .eq(IntimacyRecordPO::getCompanionId, companionId)))
                .map(this::toDomain);
    }

    @Override
    public IntimacyRecord save(IntimacyRecord record) {
        IntimacyRecordPO po = toPO(record);
        if (po.getId() == null) {
            mapper.insert(po);
            record.assignId(po.getId());
        } else {
            mapper.updateById(po);
        }
        return record;
    }

    private IntimacyRecordPO toPO(IntimacyRecord record) {
        IntimacyRecordPO po = new IntimacyRecordPO();
        po.setId(record.getId());
        po.setPatronId(record.getPatronId());
        po.setCompanionId(record.getCompanionId());
        po.setLevelCode(record.getLevelCode());
        po.setProgressValue(record.getProgressValue());
        po.setLastInteractionAt(record.getLastInteractionAt());
        return po;
    }

    private IntimacyRecord toDomain(IntimacyRecordPO po) {
        return IntimacyRecord.restore(po.getId(), po.getPatronId(), po.getCompanionId(),
                po.getLevelCode(), po.getProgressValue(), po.getLastInteractionAt());
    }
}
