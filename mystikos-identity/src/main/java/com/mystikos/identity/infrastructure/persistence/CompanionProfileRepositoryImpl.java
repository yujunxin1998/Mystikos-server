package com.mystikos.identity.infrastructure.persistence;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mystikos.common.result.PageResult;
import com.mystikos.identity.domain.model.CompanionIdentityStatus;
import com.mystikos.identity.domain.model.CompanionProfile;
import com.mystikos.identity.domain.model.CompanionStats;
import com.mystikos.identity.domain.model.CompanionStatus;
import com.mystikos.identity.domain.model.CompanionSummary;
import com.mystikos.identity.domain.repository.CompanionProfileRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class CompanionProfileRepositoryImpl implements CompanionProfileRepository {

    private final CompanionProfileMapper companionProfileMapper;
    private final CompanionProfileTagMapper companionProfileTagMapper;
    private final CompanionQueryMapper companionQueryMapper;

    public CompanionProfileRepositoryImpl(CompanionProfileMapper companionProfileMapper,
                                           CompanionProfileTagMapper companionProfileTagMapper,
                                           CompanionQueryMapper companionQueryMapper) {
        this.companionProfileMapper = companionProfileMapper;
        this.companionProfileTagMapper = companionProfileTagMapper;
        this.companionQueryMapper = companionQueryMapper;
    }

    @Override
    @Transactional
    public CompanionProfile save(CompanionProfile profile) {
        CompanionProfilePO po = toPO(profile);
        if (companionProfileMapper.selectById(po.getUserId()) == null) {
            companionProfileMapper.insert(po);
        } else {
            companionProfileMapper.updateById(po);
        }
        companionProfileTagMapper.deleteByUserId(po.getUserId());
        for (Long tagId : profile.getTagIds()) {
            companionProfileTagMapper.insert(po.getUserId(), tagId);
        }
        return profile;
    }

    @Override
    public Optional<CompanionProfile> findByUserId(Long userId) {
        CompanionProfilePO po = companionProfileMapper.selectById(userId);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public PageResult<CompanionSummary> search(int pageNum, int pageSize, CompanionStatus status, String keyword,
                                                OffsetDateTime createdFrom, OffsetDateTime createdTo) {
        PageHelper.startPage(pageNum, pageSize);
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        List<CompanionRowPO> rows = companionQueryMapper.search(
                status == null ? null : status.name(),
                hasKeyword ? keyword : null,
                createdFrom, createdTo);
        PageInfo<CompanionRowPO> pageInfo = new PageInfo<>(rows);
        List<CompanionSummary> summaries = rows.stream().map(this::toSummary).toList();
        return PageResult.of(summaries, pageInfo.getTotal(), pageNum, pageSize);
    }

    @Override
    public CompanionStats getStats() {
        return new CompanionStats(
                companionQueryMapper.countTotal(),
                companionQueryMapper.countAvailable(),
                companionQueryMapper.countBusy(),
                companionQueryMapper.avgHourlyRate());
    }

    private CompanionProfilePO toPO(CompanionProfile profile) {
        CompanionProfilePO po = new CompanionProfilePO();
        po.setUserId(profile.getUserId());
        po.setLevel(profile.getLevel());
        po.setHourlyRate(profile.getHourlyRate());
        po.setCompanionStatus(profile.getStatus().name());
        po.setIdentityStatus(profile.getIdentityStatus().name());
        po.setIdCardNo(profile.getIdCardNo());
        po.setBankAccountName(profile.getBankAccountName());
        po.setBankAccountNo(profile.getBankAccountNo());
        po.setBankName(profile.getBankName());
        po.setCreatedAt(profile.getCreatedAt());
        return po;
    }

    private CompanionProfile toDomain(CompanionProfilePO po) {
        Set<Long> tagIds = new HashSet<>(companionProfileTagMapper.selectTagIdsByUserId(po.getUserId()));
        return CompanionProfile.restore(po.getUserId(), po.getLevel(), tagIds, po.getHourlyRate(),
                CompanionStatus.valueOf(po.getCompanionStatus()),
                po.getIdentityStatus() == null ? CompanionIdentityStatus.ACTIVE
                        : CompanionIdentityStatus.valueOf(po.getIdentityStatus()),
                po.getIdCardNo(), po.getBankAccountName(), po.getBankAccountNo(), po.getBankName(),
                po.getCreatedAt());
    }

    private CompanionSummary toSummary(CompanionRowPO row) {
        Set<Long> tagIds = new HashSet<>(companionProfileTagMapper.selectTagIdsByUserId(row.getUserId()));
        return new CompanionSummary(row.getUserId(), row.getPhone(), row.getEmail(), row.getNickname(),
                row.getAvatarUrl(), CompanionStatus.valueOf(row.getCompanionStatus()), row.getLevel(),
                tagIds, row.getHourlyRate(), row.getIdCardNo(), row.getBankAccountName(),
                row.getBankAccountNo(), row.getBankName(), row.getCreatedAt());
    }
}
