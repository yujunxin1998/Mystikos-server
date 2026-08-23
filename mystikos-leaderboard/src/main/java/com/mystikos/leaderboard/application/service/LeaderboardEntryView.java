package com.mystikos.leaderboard.application.service;

import java.math.BigDecimal;

public record LeaderboardEntryView(int rank, Long subjectId, BigDecimal value) {
}
