package com.mystikos.relationship.application.service;

import com.mystikos.common.event.DomainEventPublisher;
import com.mystikos.relationship.domain.model.IntimacyDailyAccrual;
import com.mystikos.relationship.domain.model.IntimacyLevelDefinition;
import com.mystikos.relationship.domain.model.IntimacyRecord;
import com.mystikos.relationship.domain.model.RelationshipSettings;
import com.mystikos.relationship.domain.repository.IntimacyDailyAccrualRepository;
import com.mystikos.relationship.domain.repository.IntimacyLevelDefinitionRepository;
import com.mystikos.relationship.domain.repository.IntimacyRecordRepository;
import com.mystikos.relationship.domain.repository.RelationshipSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 覆盖每日上限裁剪——这是秘典明确要求的防刷机制，最容易被忽略的边界情况。 */
class RelationshipApplicationServiceTest {

    private IntimacyRecordRepository intimacyRecordRepository;
    private IntimacyDailyAccrualRepository dailyAccrualRepository;
    private RelationshipApplicationService service;

    @BeforeEach
    void setUp() {
        intimacyRecordRepository = mock(IntimacyRecordRepository.class);
        IntimacyLevelDefinitionRepository levelDefinitionRepository = mock(IntimacyLevelDefinitionRepository.class);
        dailyAccrualRepository = mock(IntimacyDailyAccrualRepository.class);
        RelationshipSettingsRepository settingsRepository = mock(RelationshipSettingsRepository.class);
        DomainEventPublisher eventPublisher = mock(DomainEventPublisher.class);

        service = new RelationshipApplicationService(intimacyRecordRepository, levelDefinitionRepository,
                dailyAccrualRepository, settingsRepository, eventPublisher);

        when(levelDefinitionRepository.findAll()).thenReturn(List.of(
                new IntimacyLevelDefinition(1L, "VEILED_ENCOUNTER", "初遇", "Veiled Encounter", BigDecimal.ZERO, null, 1),
                new IntimacyLevelDefinition(2L, "FAINT_GLIMMER", "微光", "Faint Glimmer", BigDecimal.valueOf(300), null, 2)));
        when(settingsRepository.get()).thenReturn(new RelationshipSettings(BigDecimal.valueOf(1000)));
        when(intimacyRecordRepository.findByPatronAndCompanion(any(), any())).thenReturn(Optional.empty());
        when(intimacyRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0, IntimacyRecord.class));
        when(dailyAccrualRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0, IntimacyDailyAccrual.class));
    }

    @Test
    void accrueProgressWithinCapAppliesInFull() {
        when(dailyAccrualRepository.findByKey(1L, 2L, LocalDate.now()))
                .thenReturn(Optional.of(IntimacyDailyAccrual.initiate(1L, 2L, LocalDate.now())));

        BigDecimal applied = service.accrueProgress(1L, 2L, BigDecimal.valueOf(500));

        assertThat(applied).isEqualByComparingTo("500");
    }

    @Test
    void accrueProgressBeyondDailyCapIsClipped() {
        IntimacyDailyAccrual alreadyAccrued = IntimacyDailyAccrual.initiate(1L, 2L, LocalDate.now());
        alreadyAccrued.accrueUpTo(BigDecimal.valueOf(900), BigDecimal.valueOf(1000));
        when(dailyAccrualRepository.findByKey(1L, 2L, LocalDate.now())).thenReturn(Optional.of(alreadyAccrued));

        // 已经用掉 900/1000，这次请求 500，只应该再计入 100
        BigDecimal applied = service.accrueProgress(1L, 2L, BigDecimal.valueOf(500));

        assertThat(applied).isEqualByComparingTo("100");
    }
}
