package com.mystikos.relationship.domain.repository;

import com.mystikos.relationship.domain.model.IntimacyDailyAccrual;

import java.time.LocalDate;
import java.util.Optional;

public interface IntimacyDailyAccrualRepository {

    Optional<IntimacyDailyAccrual> findByKey(Long patronId, Long companionId, LocalDate statDate);

    IntimacyDailyAccrual save(IntimacyDailyAccrual accrual);
}
