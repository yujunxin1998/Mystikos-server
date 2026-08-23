package com.mystikos.relationship.application.service;

import com.mystikos.common.event.DomainEventPublisher;
import com.mystikos.relationship.domain.event.IntimacyStageChangedEvent;
import com.mystikos.relationship.domain.model.IntimacyRecord;
import com.mystikos.relationship.domain.repository.IntimacyRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 亲密度只对外暴露只读查询 + 累加进度这两个用例，没有独立的"创建关系"接口——
 * 关系是双方互动（赠礼/预约完成）的副产物，见 docs/architecture/domain-model.md。
 */
@Service
public class RelationshipApplicationService {

    private final IntimacyRecordRepository intimacyRecordRepository;
    private final DomainEventPublisher eventPublisher;

    public RelationshipApplicationService(IntimacyRecordRepository intimacyRecordRepository,
                                           DomainEventPublisher eventPublisher) {
        this.intimacyRecordRepository = intimacyRecordRepository;
        this.eventPublisher = eventPublisher;
    }

    public IntimacyView getIntimacy(Long patronId, Long companionId) {
        return intimacyRecordRepository.findByPatronAndCompanion(patronId, companionId)
                .map(r -> new IntimacyView(r.getPatronId(), r.getCompanionId(), r.getStage(), r.getProgressValue()))
                .orElseGet(() -> IntimacyView.empty(patronId, companionId));
    }

    @Transactional
    public void accrueProgress(Long patronId, Long companionId, BigDecimal amount) {
        IntimacyRecord record = intimacyRecordRepository.findByPatronAndCompanion(patronId, companionId)
                .orElseGet(() -> IntimacyRecord.initiate(patronId, companionId));
        int previousStage = record.getStage();
        boolean stageChanged = record.accrueProgress(amount);
        intimacyRecordRepository.save(record);
        if (stageChanged) {
            eventPublisher.publish(new IntimacyStageChangedEvent(
                    patronId, companionId, previousStage, record.getStage()));
        }
    }
}
