package com.mystikos.leaderboard.adapter.web.dto;

import com.mystikos.leaderboard.application.service.LeaderboardEntryView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "榜单条目")
public class LeaderboardEntryResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "名次，从 1 开始")
    private Integer rank;

    @Schema(description = "陪玩或老板用户ID")
    private Long subjectId;

    @Schema(description = "魅力值或守护值")
    private BigDecimal value;

    public static LeaderboardEntryResponse from(LeaderboardEntryView view) {
        LeaderboardEntryResponse dto = new LeaderboardEntryResponse();
        dto.setRank(view.rank());
        dto.setSubjectId(view.subjectId());
        dto.setValue(view.value());
        return dto;
    }
}
