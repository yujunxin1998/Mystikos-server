package com.mystikos.relationship.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mystikos.relationship.domain.model.IntimacyDailyAccrual;
import com.mystikos.relationship.domain.repository.IntimacyDailyAccrualRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public class IntimacyDailyAccrualRepositoryImpl implements IntimacyDailyAccrualRepository {

    private final IntimacyDailyAccrualMapper mapper;

    public IntimacyDailyAccrualRepositoryImpl(IntimacyDailyAccrualMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<IntimacyDailyAccrual> findByKey(Long patronId, Long companionId, LocalDate statDate) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<IntimacyDailyAccrualPO>lambdaQuery()
                        .eq(IntimacyDailyAccrualPO::getPatronId, patronId)
                        .eq(IntimacyDailyAccrualPO::getCompanionId, companionId)
                        .eq(IntimacyDailyAccrualPO::getStatDate, statDate)))
                .map(this::toDomain);
    }

    @Override
    public IntimacyDailyAccrual save(IntimacyDailyAccrual accrual) {
        IntimacyDailyAccrualPO po = toPO(accrual);
        if (po.getId() == null) {
            mapper.insert(po);
            accrual.assignId(po.getId());
        } else {
            mapper.updateById(po);
        }
        return accrual;
    }

    private IntimacyDailyAccrualPO toPO(IntimacyDailyAccrual accrual) {
        IntimacyDailyAccrualPO po = new IntimacyDailyAccrualPO();
        po.setId(accrual.getId());
        po.setPatronId(accrual.getPatronId());
        po.setCompanionId(accrual.getCompanionId());
        po.setStatDate(accrual.getStatDate());
        po.setAccrued(accrual.getAccrued());
        return po;
    }

    private IntimacyDailyAccrual toDomain(IntimacyDailyAccrualPO po) {
        return IntimacyDailyAccrual.restore(po.getId(), po.getPatronId(), po.getCompanionId(),
                po.getStatDate(), po.getAccrued());
    }
}
