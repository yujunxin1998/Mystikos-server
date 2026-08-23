package com.mystikos.relationship.domain.model;

import java.math.BigDecimal;

/**
 * 亲密度进度 → 阶段的映射规则。阈值是占位值（Lv0-4 对应原型的 5 段进度条），
 * 业务确认真实门槛后改这里的常量即可，不用动调用方（见需求讨论：先占位，后续随时可改）。
 */
public final class IntimacyStagePolicy {

    /** 下标即阶段号，值是进入该阶段所需的最低累计进度。 */
    private static final BigDecimal[] STAGE_THRESHOLDS = {
            BigDecimal.valueOf(0),      // Stage 0 陌生
            BigDecimal.valueOf(100),    // Stage 1 相识
            BigDecimal.valueOf(500),    // Stage 2 熟悉
            BigDecimal.valueOf(2000),   // Stage 3 亲密
            BigDecimal.valueOf(8000),   // Stage 4 挚友
    };

    public static final int MAX_STAGE = STAGE_THRESHOLDS.length - 1;

    private IntimacyStagePolicy() {
    }

    public static int resolveStage(BigDecimal progressValue) {
        int stage = 0;
        for (int i = STAGE_THRESHOLDS.length - 1; i >= 0; i--) {
            if (progressValue.compareTo(STAGE_THRESHOLDS[i]) >= 0) {
                stage = i;
                break;
            }
        }
        return stage;
    }
}
