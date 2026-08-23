package com.mystikos.leaderboard.infrastructure.persistence;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mystikos.leaderboard.domain.model.PatronGuardStat;
import com.mystikos.leaderboard.domain.repository.PatronGuardStatRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PatronGuardStatRepositoryImpl implements PatronGuardStatRepository {

    private final PatronGuardStatMapper mapper;

    public PatronGuardStatRepositoryImpl(PatronGuardStatMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<PatronGuardStat> findByPatronId(Long patronId) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<PatronGuardStatPO>lambdaQuery()
                        .eq(PatronGuardStatPO::getPatronId, patronId)))
                .map(po -> PatronGuardStat.restore(po.getPatronId(), po.getGuardValue()));
    }

    @Override
    public PatronGuardStat save(PatronGuardStat stat) {
        PatronGuardStatPO existing = mapper.selectOne(Wrappers.<PatronGuardStatPO>lambdaQuery()
                .eq(PatronGuardStatPO::getPatronId, stat.getPatronId()));
        PatronGuardStatPO po = new PatronGuardStatPO();
        po.setId(existing == null ? null : existing.getId());
        po.setPatronId(stat.getPatronId());
        po.setGuardValue(stat.getGuardValue());
        if (po.getId() == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
        return stat;
    }

    @Override
    public List<PatronGuardStat> findTopN(int limit) {
        return mapper.selectList(Wrappers.<PatronGuardStatPO>lambdaQuery()
                        .orderByDesc(PatronGuardStatPO::getGuardValue)
                        .last("LIMIT " + limit))
                .stream()
                .map(po -> PatronGuardStat.restore(po.getPatronId(), po.getGuardValue()))
                .toList();
    }
}
