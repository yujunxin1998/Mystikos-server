package com.mystikos.gifting.infrastructure.persistence;

import com.mystikos.gifting.domain.model.GiftTransaction;
import com.mystikos.gifting.domain.model.GiftTransactionStatus;
import com.mystikos.gifting.domain.repository.GiftTransactionRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public class GiftTransactionRepositoryImpl implements GiftTransactionRepository {

    private final GiftTransactionMapper mapper;

    public GiftTransactionRepositoryImpl(GiftTransactionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GiftTransaction save(GiftTransaction transaction) {
        GiftTransactionPO po = toPO(transaction);
        if (po.getId() == null) {
            mapper.insert(po);
            transaction.assignId(po.getId());
        } else {
            mapper.updateById(po);
        }
        return transaction;
    }

    @Override
    public Optional<GiftTransaction> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
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
        po.setTierMultiplierSnapshot(transaction.getTierMultiplierSnapshot());
        po.setIntimacyValue(transaction.getIntimacyValue());
        po.setSentAt(transaction.getSentAt());
        po.setStatus(transaction.getStatus().name());
        return po;
    }

    private GiftTransaction toDomain(GiftTransactionPO po) {
        return GiftTransaction.restore(po.getId(), po.getPatronId(), po.getCompanionId(), po.getGiftId(),
                po.getQuantity(), po.getAmount(), po.getTierMultiplierSnapshot(), po.getIntimacyValue(),
                po.getSentAt(), GiftTransactionStatus.valueOf(po.getStatus()));
    }
}
