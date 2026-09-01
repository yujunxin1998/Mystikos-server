package com.mystikos.relationship.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IntimacyRecordTest {

    private final List<IntimacyLevelDefinition> levels = List.of(
            new IntimacyLevelDefinition(1L, "VEILED_ENCOUNTER", "初遇", "Veiled Encounter", BigDecimal.ZERO, null, 1),
            new IntimacyLevelDefinition(2L, "FAINT_GLIMMER", "微光", "Faint Glimmer", BigDecimal.valueOf(300), null, 2),
            new IntimacyLevelDefinition(3L, "RESONANCE", "共鸣", "Resonance", BigDecimal.valueOf(1000), null, 3));

    @Test
    void accrueProgressAdvancesLevelWhenThresholdCrossed() {
        IntimacyRecord record = IntimacyRecord.initiate(1L, 2L);

        boolean changed = record.accrueProgress(BigDecimal.valueOf(300), levels);

        assertThat(changed).isTrue();
        assertThat(record.getLevelCode()).isEqualTo("FAINT_GLIMMER");
        assertThat(record.getProgressValue()).isEqualByComparingTo("300");
    }

    @Test
    void accrueProgressWithinSameLevelDoesNotFireChange() {
        IntimacyRecord record = IntimacyRecord.initiate(1L, 2L);
        record.accrueProgress(BigDecimal.valueOf(300), levels);

        boolean changed = record.accrueProgress(BigDecimal.valueOf(50), levels);

        assertThat(changed).isFalse();
        assertThat(record.getLevelCode()).isEqualTo("FAINT_GLIMMER");
    }

    @Test
    void reverseProgressFloorsAtZeroAndAllowsDowngrade() {
        IntimacyRecord record = IntimacyRecord.initiate(1L, 2L);
        record.accrueProgress(BigDecimal.valueOf(1000), levels);
        assertThat(record.getLevelCode()).isEqualTo("RESONANCE");

        boolean changed = record.reverseProgress(BigDecimal.valueOf(5000), levels);

        assertThat(changed).isTrue();
        assertThat(record.getProgressValue()).isEqualByComparingTo("0");
        assertThat(record.getLevelCode()).isEqualTo("VEILED_ENCOUNTER");
    }
}
