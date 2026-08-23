package com.mystikos.leaderboard.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mystikos.leaderboard.domain.model.CompanionCharmStat;
import com.mystikos.leaderboard.domain.repository.CompanionCharmStatRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CompanionCharmStatRepositoryImpl implements CompanionCharmStatRepository {

    private final CompanionCharmStatMapper mapper;

    public CompanionCharmStatRepositoryImpl(CompanionCharmStatMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<CompanionCharmStat> findByCompanionId(Long companionId) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<CompanionCharmStatPO>lambdaQuery()
                        .eq(CompanionCharmStatPO::getCompanionId, companionId)))
                .map(po -> CompanionCharmStat.restore(po.getCompanionId(), po.getCharmValue()));
    }

    @Override
    public CompanionCharmStat save(CompanionCharmStat stat) {
        CompanionCharmStatPO existing = mapper.selectOne(Wrappers.<CompanionCharmStatPO>lambdaQuery()
                .eq(CompanionCharmStatPO::getCompanionId, stat.getCompanionId()));
        CompanionCharmStatPO po = new CompanionCharmStatPO();
        po.setId(existing == null ? null : existing.getId());
        po.setCompanionId(stat.getCompanionId());
        po.setCharmValue(stat.getCharmValue());
        if (po.getId() == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
        return stat;
    }

    @Override
    public List<CompanionCharmStat> findTopN(int limit) {
        return mapper.selectList(Wrappers.<CompanionCharmStatPO>lambdaQuery()
                        .orderByDesc(CompanionCharmStatPO::getCharmValue)
                        .last("LIMIT " + limit))
                .stream()
                .map(po -> CompanionCharmStat.restore(po.getCompanionId(), po.getCharmValue()))
                .toList();
    }
}
