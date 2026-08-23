package com.mystikos.leaderboard.domain.repository;

import com.mystikos.leaderboard.domain.model.PatronGuardStat;

import java.util.List;
import java.util.Optional;

public interface PatronGuardStatRepository {

    Optional<PatronGuardStat> findByPatronId(Long patronId);

    PatronGuardStat save(PatronGuardStat stat);

    List<PatronGuardStat> findTopN(int limit);
}
