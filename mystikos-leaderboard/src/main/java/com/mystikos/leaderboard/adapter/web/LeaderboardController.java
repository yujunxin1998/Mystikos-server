package com.mystikos.leaderboard.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.leaderboard.adapter.web.dto.LeaderboardEntryResponse;
import com.mystikos.leaderboard.application.service.LeaderboardApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 纯读侧投影，排名实时计算（见 LeaderboardApplicationService 的说明），
 * 累加只由事件驱动，这里不提供任何写接口。
 */
@RestController
@RequestMapping("/api/v1/leaderboard")
@Tag(name = "排行榜", description = "陪玩魅力榜 / 老板守护榜，实时计算")
public class LeaderboardController {

    private static final int MAX_LIMIT = 100;

    private final LeaderboardApplicationService leaderboardApplicationService;

    public LeaderboardController(LeaderboardApplicationService leaderboardApplicationService) {
        this.leaderboardApplicationService = leaderboardApplicationService;
    }

    @GetMapping("/companions")
    @Operation(summary = "陪玩魅力榜", description = "按累计魅力值倒序，limit 最大 100")
    public APIResponse<List<LeaderboardEntryResponse>> companions(
            @Parameter(description = "取前 N 名，默认 50") @RequestParam(defaultValue = "50") int limit) {
        return APIResponse.ok(leaderboardApplicationService.topCompanions(clamp(limit)).stream()
                .map(LeaderboardEntryResponse::from)
                .toList());
    }

    @GetMapping("/patrons")
    @Operation(summary = "老板守护榜", description = "按累计守护值倒序，limit 最大 100")
    public APIResponse<List<LeaderboardEntryResponse>> patrons(
            @Parameter(description = "取前 N 名，默认 50") @RequestParam(defaultValue = "50") int limit) {
        return APIResponse.ok(leaderboardApplicationService.topPatrons(clamp(limit)).stream()
                .map(LeaderboardEntryResponse::from)
                .toList());
    }

    private int clamp(int limit) {
        if (limit <= 0) {
            return 50;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
