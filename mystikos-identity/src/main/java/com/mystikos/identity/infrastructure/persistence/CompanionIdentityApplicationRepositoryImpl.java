package com.mystikos.identity.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mystikos.common.result.PageResult;
import com.mystikos.identity.domain.model.AssessmentResult;
import com.mystikos.identity.domain.model.CompanionIdentityApplication;
import com.mystikos.identity.domain.model.CompanionIdentityApplicationStatus;
import com.mystikos.identity.domain.model.Gender;
import com.mystikos.identity.domain.repository.CompanionIdentityApplicationRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class CompanionIdentityApplicationRepositoryImpl implements CompanionIdentityApplicationRepository {

    private static final List<String> ACTIVE_STATUSES = List.of(
            CompanionIdentityApplicationStatus.SUBMITTED.name(),
            CompanionIdentityApplicationStatus.IN_ASSESSMENT.name());

    private final CompanionIdentityApplicationMapper mapper;
    private final CompanionIdentityApplicationTagMapper tagMapper;

    public CompanionIdentityApplicationRepositoryImpl(CompanionIdentityApplicationMapper mapper,
                                                        CompanionIdentityApplicationTagMapper tagMapper) {
        this.mapper = mapper;
        this.tagMapper = tagMapper;
    }

    @Override
    @Transactional
    public CompanionIdentityApplication save(CompanionIdentityApplication application) {
        CompanionIdentityApplicationPO po = toPO(application);
        if (po.getId() == null) {
            mapper.insert(po);
            application.assignId(po.getId());
        } else {
            mapper.updateById(po);
        }
        tagMapper.deleteByApplicationId(po.getId());
        for (Long tagId : application.getTagIds()) {
            tagMapper.insert(po.getId(), tagId);
        }
        return application;
    }

    @Override
    public Optional<CompanionIdentityApplication> findById(Long id) {
        CompanionIdentityApplicationPO po = mapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Optional<CompanionIdentityApplication> findLatestByUserId(Long userId) {
        List<CompanionIdentityApplicationPO> pos = mapper.selectList(
                new LambdaQueryWrapper<CompanionIdentityApplicationPO>()
                        .eq(CompanionIdentityApplicationPO::getUserId, userId)
                        .orderByDesc(CompanionIdentityApplicationPO::getCreatedAt)
                        .last("LIMIT 1"));
        return pos.isEmpty() ? Optional.empty() : Optional.of(toDomain(pos.get(0)));
    }

    @Override
    public boolean existsActiveByUserId(Long userId) {
        return mapper.exists(new LambdaQueryWrapper<CompanionIdentityApplicationPO>()
                .eq(CompanionIdentityApplicationPO::getUserId, userId)
                .in(CompanionIdentityApplicationPO::getStatus, ACTIVE_STATUSES));
    }

    @Override
    public PageResult<CompanionIdentityApplication> search(int pageNum, int pageSize,
                                                             CompanionIdentityApplicationStatus status,
                                                             String keyword, OffsetDateTime createdFrom,
                                                             OffsetDateTime createdTo) {
        PageHelper.startPage(pageNum, pageSize);
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        LambdaQueryWrapper<CompanionIdentityApplicationPO> wrapper =
                new LambdaQueryWrapper<CompanionIdentityApplicationPO>()
                        .eq(status != null, CompanionIdentityApplicationPO::getStatus,
                                status == null ? null : status.name())
                        .ge(createdFrom != null, CompanionIdentityApplicationPO::getCreatedAt, createdFrom)
                        .le(createdTo != null, CompanionIdentityApplicationPO::getCreatedAt, createdTo)
                        .and(hasKeyword, w -> w.like(CompanionIdentityApplicationPO::getRealName, keyword)
                                .or().like(CompanionIdentityApplicationPO::getGameNickname, keyword)
                                .or().like(CompanionIdentityApplicationPO::getContactPhone, keyword)
                                .or().like(CompanionIdentityApplicationPO::getContactEmail, keyword))
                        .orderByDesc(CompanionIdentityApplicationPO::getCreatedAt);
        List<CompanionIdentityApplicationPO> pos = mapper.selectList(wrapper);
        PageInfo<CompanionIdentityApplicationPO> pageInfo = new PageInfo<>(pos);
        List<CompanionIdentityApplication> applications = pos.stream().map(this::toDomain).toList();
        return PageResult.of(applications, pageInfo.getTotal(), pageNum, pageSize);
    }

    private CompanionIdentityApplicationPO toPO(CompanionIdentityApplication app) {
        CompanionIdentityApplicationPO po = new CompanionIdentityApplicationPO();
        po.setId(app.getId());
        po.setUserId(app.getUserId());
        po.setRealName(app.getRealName());
        po.setGender(app.getGender() == null ? null : app.getGender().name());
        po.setBirthDate(app.getBirthDate());
        po.setSelfIntro(app.getSelfIntro());
        po.setGameNickname(app.getGameNickname());
        po.setGameRankProofObjectKey(app.getGameRankProofObjectKey());
        po.setContactCountryCode(app.getContactCountryCode());
        po.setContactPhone(app.getContactPhone());
        po.setContactEmail(app.getContactEmail());
        po.setStatus(app.getStatus().name());
        po.setReviewerId(app.getReviewerId());
        po.setReviewResult(app.getReviewResult() == null ? null : app.getReviewResult().name());
        po.setReviewComment(app.getReviewComment());
        po.setReviewedAt(app.getReviewedAt());
        po.setCreatedAt(app.getCreatedAt());
        po.setUpdatedAt(app.getUpdatedAt());
        return po;
    }

    private CompanionIdentityApplication toDomain(CompanionIdentityApplicationPO po) {
        Set<Long> tagIds = new HashSet<>(tagMapper.selectTagIdsByApplicationId(po.getId()));
        return CompanionIdentityApplication.restore(po.getId(), po.getUserId(), po.getRealName(),
                po.getGender() == null ? null : Gender.valueOf(po.getGender()), po.getBirthDate(),
                po.getSelfIntro(), po.getGameNickname(), po.getGameRankProofObjectKey(), tagIds,
                po.getContactCountryCode(), po.getContactPhone(), po.getContactEmail(),
                CompanionIdentityApplicationStatus.valueOf(po.getStatus()), po.getReviewerId(),
                po.getReviewResult() == null ? null : AssessmentResult.valueOf(po.getReviewResult()),
                po.getReviewComment(), po.getReviewedAt(), po.getCreatedAt(), po.getUpdatedAt());
    }
}
