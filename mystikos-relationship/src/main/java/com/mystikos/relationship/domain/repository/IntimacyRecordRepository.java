package com.mystikos.relationship.domain.repository;

import com.mystikos.relationship.domain.model.IntimacyRecord;

import java.util.Optional;

public interface IntimacyRecordRepository {

    Optional<IntimacyRecord> findByPatronAndCompanion(Long patronId, Long companionId);

    IntimacyRecord save(IntimacyRecord record);
}
