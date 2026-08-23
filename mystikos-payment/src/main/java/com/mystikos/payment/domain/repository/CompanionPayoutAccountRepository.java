package com.mystikos.payment.domain.repository;

import com.mystikos.payment.domain.model.CompanionPayoutAccount;

import java.util.Optional;

public interface CompanionPayoutAccountRepository {

    CompanionPayoutAccount save(CompanionPayoutAccount account);

    Optional<CompanionPayoutAccount> findByUserId(Long userId);
}
