package com.mystikos.payment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mystikos.payment.domain.model.CompanionPayoutAccount;
import com.mystikos.payment.domain.repository.CompanionPayoutAccountRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CompanionPayoutAccountRepositoryImpl implements CompanionPayoutAccountRepository {

    private final CompanionPayoutAccountMapper mapper;

    public CompanionPayoutAccountRepositoryImpl(CompanionPayoutAccountMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public CompanionPayoutAccount save(CompanionPayoutAccount account) {
        CompanionPayoutAccountPO po = new CompanionPayoutAccountPO();
        po.setId(account.getId());
        po.setUserId(account.getUserId());
        po.setStripeConnectAccountId(account.getStripeConnectAccountId());
        if (po.getId() == null) {
            mapper.insert(po);
            account.assignId(po.getId());
        } else {
            mapper.updateById(po);
        }
        return account;
    }

    @Override
    public Optional<CompanionPayoutAccount> findByUserId(Long userId) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<CompanionPayoutAccountPO>lambdaQuery()
                        .eq(CompanionPayoutAccountPO::getUserId, userId)))
                .map(po -> CompanionPayoutAccount.restore(po.getId(), po.getUserId(), po.getStripeConnectAccountId()));
    }
}
