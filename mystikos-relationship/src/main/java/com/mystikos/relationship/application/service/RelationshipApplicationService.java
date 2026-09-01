package com.mystikos.relationship.application.service;

import com.mystikos.common.event.DomainEventPublisher;
import com.mystikos.relationship.application.command.SaveIntimacyLevelCommand;
import com.mystikos.relationship.domain.event.IntimacyLevelChangedEvent;
import com.mystikos.relationship.domain.model.IntimacyDailyAccrual;
import com.mystikos.relationship.domain.model.IntimacyLevelDefinition;
import com.mystikos.relationship.domain.model.IntimacyRecord;
import com.mystikos.relationship.domain.model.RelationshipSettings;
import com.mystikos.relationship.domain.repository.IntimacyDailyAccrualRepository;
import com.mystikos.relationship.domain.repository.IntimacyLevelDefinitionRepository;
import com.mystikos.relationship.domain.repository.IntimacyRecordRepository;
import com.mystikos.relationship.domain.repository.RelationshipSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 亲密度只对外暴露只读查询 + 累加/回滚进度这两类用例，没有独立的"创建关系"接口——
 * 关系是双方互动（赠礼/预约完成）的副产物，见 docs/architecture/domain-model.md。
 */
@Service
public class RelationshipApplicationService {

    private final IntimacyRecordRepository intimacyRecordRepository;
    private final IntimacyLevelDefinitionRepository levelDefinitionRepository;
    private final IntimacyDailyAccrualRepository dailyAccrualRepository;
    private final RelationshipSettingsRepository settingsRepository;
    private final DomainEventPublisher eventPublisher;

    public RelationshipApplicationService(IntimacyRecordRepository intimacyRecordRepository,
                                           IntimacyLevelDefinitionRepository levelDefinitionRepository,
                                           IntimacyDailyAccrualRepository dailyAccrualRepository,
                                           RelationshipSettingsRepository settingsRepository,
                                           DomainEventPublisher eventPublisher) {
        this.intimacyRecordRepository = intimacyRecordRepository;
        this.levelDefinitionRepository = levelDefinitionRepository;
        this.dailyAccrualRepository = dailyAccrualRepository;
        this.settingsRepository = settingsRepository;
        this.eventPublisher = eventPublisher;
    }

    public IntimacyView getIntimacy(Long patronId, Long companionId) {
        return intimacyRecordRepository.findByPatronAndCompanion(patronId, companionId)
                .map(r -> new IntimacyView(r.getPatronId(), r.getCompanionId(), r.getLevelCode(), r.getProgressValue()))
                .orElseGet(() -> IntimacyView.empty(patronId, companionId));
    }

    public List<IntimacyLevelDefinition> listLevels() {
        return levelDefinitionRepository.findAll();
    }

    public RelationshipSettings getSettings() {
        return settingsRepository.get();
    }

    @Transactional
    public Long saveLevel(SaveIntimacyLevelCommand command) {
        IntimacyLevelDefinition definition = new IntimacyLevelDefinition(command.id(), command.code(),
                command.displayNameZh(), command.displayNameEn(), command.threshold(),
                command.perkDescription(), command.sortOrder());
        return levelDefinitionRepository.save(definition).getId();
    }

    @Transactional
    public void updateSettings(BigDecimal dailyIntimacyCap) {
        settingsRepository.save(new RelationshipSettings(dailyIntimacyCap));
    }

    /**
     * 累加亲密度进度，先按当日已累加值裁剪到每日上限（防刷：极短时间内集中投递礼物
     * 不能无限换亲密度，但赠礼本身照常成立，钱和 VIP/排行榜不受这个上限影响）。
     * @return 实际计入 progressValue 的数值（可能小于 requestedAmount）
     */
    @Transactional
    public BigDecimal accrueProgress(Long patronId, Long companionId, BigDecimal requestedAmount) {
        LocalDate today = LocalDate.now();
        IntimacyDailyAccrual dailyAccrual = dailyAccrualRepository.findByKey(patronId, companionId, today)
                .orElseGet(() -> IntimacyDailyAccrual.initiate(patronId, companionId, today));
        BigDecimal dailyCap = settingsRepository.get().getDailyIntimacyCap();
        BigDecimal applied = dailyAccrual.accrueUpTo(requestedAmount, dailyCap);
        dailyAccrualRepository.save(dailyAccrual);

        if (applied.signum() > 0) {
            applyProgressChange(patronId, companionId, applied, true);
        }
        return applied;
    }

    /**
     * 赠礼退款：扣减对应的亲密度进度值，floor 在 0，允许降级——和正常累加的单调性要求
     * 不同，只在退款这一条路径上生效。不经过每日上限裁剪（上限只限制"能涨多快"，不限制
     * "能退多少"）。
     */
    @Transactional
    public void reverseProgress(Long patronId, Long companionId, BigDecimal amount) {
        if (amount.signum() <= 0) {
            return;
        }
        applyProgressChange(patronId, companionId, amount, false);
    }

    private void applyProgressChange(Long patronId, Long companionId, BigDecimal amount, boolean accrue) {
        IntimacyRecord record = intimacyRecordRepository.findByPatronAndCompanion(patronId, companionId)
                .orElseGet(() -> IntimacyRecord.initiate(patronId, companionId));
        List<IntimacyLevelDefinition> levels = levelDefinitionRepository.findAll();
        String previousCode = record.getLevelCode();
        boolean levelChanged = accrue
                ? record.accrueProgress(amount, levels)
                : record.reverseProgress(amount, levels);
        intimacyRecordRepository.save(record);
        if (levelChanged) {
            eventPublisher.publish(new IntimacyLevelChangedEvent(
                    patronId, companionId, previousCode, record.getLevelCode()));
        }
    }
}
