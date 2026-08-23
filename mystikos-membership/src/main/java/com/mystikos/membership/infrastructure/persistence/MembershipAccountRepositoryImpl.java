package com.mystikos.membership.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mystikos.membership.domain.model.DefaultMembershipTier;
import com.mystikos.membership.domain.model.MembershipAccount;
import com.mystikos.membership.domain.repository.MembershipAccountRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MembershipAccountRepositoryImpl implements MembershipAccountRepository {

    private final MembershipAccountMapper mapper;

    public MembershipAccountRepositoryImpl(MembershipAccountMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<MembershipAccount> findByPatronId(Long patronId) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<MembershipAccountPO>lambdaQuery()
                        .eq(MembershipAccountPO::getPatronId, patronId)))
                .map(this::toDomain);
    }

    @Override
    public MembershipAccount save(MembershipAccount account) {
        MembershipAccountPO po = toPO(account);
        if (po.getId() == null) {
            mapper.insert(po);
            account.assignId(po.getId());
        } else {
            mapper.updateById(po);
        }
        return account;
    }

    private MembershipAccountPO toPO(MembershipAccount account) {
        MembershipAccountPO po = new MembershipAccountPO();
        po.setId(account.getId());
        po.setPatronId(account.getPatronId());
        po.setCurrentTierCode(account.getCurrentTier().getCode());
        po.setCumulativeSpend(account.getCumulativeSpend());
        po.setTierUpgradedAt(account.getTierUpgradedAt());
        return po;
    }

    private MembershipAccount toDomain(MembershipAccountPO po) {
        return MembershipAccount.restore(po.getId(), po.getPatronId(),
                DefaultMembershipTier.valueOf(po.getCurrentTierCode()),
                po.getCumulativeSpend(), po.getTierUpgradedAt());
    }
}
