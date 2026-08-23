package com.mystikos.gifting.infrastructure.persistence;

import com.mystikos.gifting.domain.model.GiftTransaction;
import com.mystikos.gifting.domain.repository.GiftTransactionRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class GiftTransactionRepositoryImpl implements GiftTransactionRepository {

    private final GiftTransactionMapper mapper;

    public GiftTransactionRepositoryImpl(GiftTransactionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GiftTransaction save(GiftTransaction transaction) {
        GiftTransactionPO po = toPO(transaction);
        mapper.insert(po);
        transaction.assignId(po.getId());
        return transaction;
    }

    @Override
    public long sumQuantityByPatronAndGift(Long patronId, Long giftId) {
        return mapper.sumQuantityByPatronAndGift(patronId, giftId);
    }

    @Override
    public BigDecimal sumAmountByPatron(Long patronId) {
        return mapper.sumAmountByPatron(patronId);
    }

    private GiftTransactionPO toPO(GiftTransaction transaction) {
        GiftTransactionPO po = new GiftTransactionPO();
        po.setId(transaction.getId());
        po.setPatronId(transaction.getPatronId());
        po.setCompanionId(transaction.getCompanionId());
        po.setGiftId(transaction.getGiftId());
        po.setQuantity(transaction.getQuantity());
        po.setAmount(transaction.getAmount());
        po.setSentAt(transaction.getSentAt());
        return po;
    }
}
