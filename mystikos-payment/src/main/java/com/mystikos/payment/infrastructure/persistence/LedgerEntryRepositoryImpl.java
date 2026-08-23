package com.mystikos.payment.infrastructure.persistence;

import com.mystikos.payment.domain.model.LedgerEntry;
import com.mystikos.payment.domain.repository.LedgerEntryRepository;
import org.springframework.stereotype.Repository;

@Repository
public class LedgerEntryRepositoryImpl implements LedgerEntryRepository {

    private final LedgerEntryMapper mapper;

    public LedgerEntryRepositoryImpl(LedgerEntryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public LedgerEntry save(LedgerEntry entry) {
        LedgerEntryPO po = new LedgerEntryPO();
        po.setId(entry.getId());
        po.setIntentId(entry.getIntentId());
        po.setWalletId(entry.getWalletId());
        po.setDirection(entry.getDirection().name());
        po.setAmount(entry.getAmount());
        po.setCurrency(entry.getCurrency());
        po.setOccurredAt(entry.getOccurredAt());
        mapper.insert(po);
        entry.assignId(po.getId());
        return entry;
    }
}
