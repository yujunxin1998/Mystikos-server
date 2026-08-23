package com.mystikos.payment.domain.repository;

import com.mystikos.payment.domain.model.LedgerEntry;

public interface LedgerEntryRepository {

    LedgerEntry save(LedgerEntry entry);
}
