package com.mystikos.leaderboard.domain.repository;

import com.mystikos.leaderboard.domain.model.CompanionCharmStat;

import java.util.List;
import java.util.Optional;

public interface CompanionCharmStatRepository {

    Optional<CompanionCharmStat> findByCompanionId(Long companionId);

    CompanionCharmStat save(CompanionCharmStat stat);

    /** 按魅力值倒序取前 limit 名，排名由调用方按列表顺序赋值，不落库。 */
    List<CompanionCharmStat> findTopN(int limit);
}
