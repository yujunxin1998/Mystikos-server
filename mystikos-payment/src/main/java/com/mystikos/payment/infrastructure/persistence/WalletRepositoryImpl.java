package com.mystikos.payment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mystikos.payment.domain.model.Wallet;
import com.mystikos.payment.domain.repository.WalletRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public class WalletRepositoryImpl implements WalletRepository {

    private final WalletMapper mapper;

    public WalletRepositoryImpl(WalletMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<Wallet> findByUserId(Long userId) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<WalletPO>lambdaQuery()
                        .eq(WalletPO::getUserId, userId)))
                .map(this::toDomain);
    }

    @Override
    public Wallet findOrCreate(Long userId, String currency) {
        return findByUserId(userId).orElseGet(() -> {
            WalletPO po = new WalletPO();
            po.setUserId(userId);
            po.setBalance(BigDecimal.ZERO);
            po.setCurrency(currency);
            OffsetDateTime now = OffsetDateTime.now();
            po.setCreatedAt(now);
            po.setUpdatedAt(now);
            try {
                mapper.insert(po);
            } catch (DuplicateKeyException e) {
                // 两个并发请求同时给同一个用户首次开钱包，唯一索引兜底，重新查一次拿已插入的那条。
                return findByUserId(userId)
                        .orElseThrow(() -> e);
            }
            return toDomain(po);
        });
    }

    @Override
    public void credit(Long walletId, BigDecimal amount) {
        mapper.creditBalance(walletId, amount);
    }

    @Override
    public boolean debit(Long walletId, BigDecimal amount) {
        return mapper.debitBalance(walletId, amount) > 0;
    }

    private Wallet toDomain(WalletPO po) {
        return Wallet.restore(po.getId(), po.getUserId(), po.getBalance(), po.getCurrency());
    }
}
