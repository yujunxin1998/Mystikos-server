package com.mystikos.leaderboard.application.service;

import com.mystikos.leaderboard.domain.model.CompanionCharmStat;
import com.mystikos.leaderboard.domain.model.PatronGuardStat;
import com.mystikos.leaderboard.domain.repository.CompanionCharmStatRepository;
import com.mystikos.leaderboard.domain.repository.PatronGuardStatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 纯读侧 CQRS 投影：没有对外的写用例，只有事件消费（累加）和排名查询。
 * 排名实时计算，不落快照——"每周一更新"的冻结榜单留到有真实需求时再加调度任务。
 */
@Service
public class LeaderboardApplicationService {

    private final CompanionCharmStatRepository companionCharmStatRepository;
    private final PatronGuardStatRepository patronGuardStatRepository;

    public LeaderboardApplicationService(CompanionCharmStatRepository companionCharmStatRepository,
                                          PatronGuardStatRepository patronGuardStatRepository) {
        this.companionCharmStatRepository = companionCharmStatRepository;
        this.patronGuardStatRepository = patronGuardStatRepository;
    }

    @Transactional
    public void accrueCharm(Long companionId, BigDecimal amount) {
        CompanionCharmStat stat = companionCharmStatRepository.findByCompanionId(companionId)
                .orElseGet(() -> CompanionCharmStat.initiate(companionId));
        stat.accrue(amount);
        companionCharmStatRepository.save(stat);
    }

    @Transactional
    public void accrueGuard(Long patronId, BigDecimal amount) {
        PatronGuardStat stat = patronGuardStatRepository.findByPatronId(patronId)
                .orElseGet(() -> PatronGuardStat.initiate(patronId));
        stat.accrue(amount);
        patronGuardStatRepository.save(stat);
    }

    public List<LeaderboardEntryView> topCompanions(int limit) {
        List<CompanionCharmStat> stats = companionCharmStatRepository.findTopN(limit);
        List<LeaderboardEntryView> result = new ArrayList<>(stats.size());
        for (int i = 0; i < stats.size(); i++) {
            result.add(new LeaderboardEntryView(i + 1, stats.get(i).getCompanionId(), stats.get(i).getCharmValue()));
        }
        return result;
    }

    public List<LeaderboardEntryView> topPatrons(int limit) {
        List<PatronGuardStat> stats = patronGuardStatRepository.findTopN(limit);
        List<LeaderboardEntryView> result = new ArrayList<>(stats.size());
        for (int i = 0; i < stats.size(); i++) {
            result.add(new LeaderboardEntryView(i + 1, stats.get(i).getPatronId(), stats.get(i).getGuardValue()));
        }
        return result;
    }
}
