package com.mystikos.identity.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mystikos.common.result.PageResult;
import com.mystikos.identity.domain.model.CompanionShowcase;
import com.mystikos.identity.domain.model.CompanionShowcaseMediaType;
import com.mystikos.identity.domain.model.CompanionShowcasePublicSummary;
import com.mystikos.identity.domain.repository.CompanionShowcaseRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class CompanionShowcaseRepositoryImpl implements CompanionShowcaseRepository {

    private final CompanionShowcaseMapper mapper;
    private final CompanionShowcasePublicQueryMapper publicQueryMapper;
    private final CompanionShowcaseRevisionTagMapper revisionTagMapper;
    private final CompanionShowcaseRevisionMediaMapper revisionMediaMapper;

    public CompanionShowcaseRepositoryImpl(CompanionShowcaseMapper mapper,
                                            CompanionShowcasePublicQueryMapper publicQueryMapper,
                                            CompanionShowcaseRevisionTagMapper revisionTagMapper,
                                            CompanionShowcaseRevisionMediaMapper revisionMediaMapper) {
        this.mapper = mapper;
        this.publicQueryMapper = publicQueryMapper;
        this.revisionTagMapper = revisionTagMapper;
        this.revisionMediaMapper = revisionMediaMapper;
    }

    @Override
    @Transactional
    public CompanionShowcase save(CompanionShowcase showcase) {
        CompanionShowcasePO po = toPO(showcase);
        if (mapper.selectById(po.getUserId()) == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
        return showcase;
    }

    @Override
    public Optional<CompanionShowcase> findByUserId(Long userId) {
        CompanionShowcasePO po = mapper.selectById(userId);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public PageResult<CompanionShowcasePublicSummary> searchPublished(int pageNum, int pageSize, Long tagId,
                                                                       String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        List<CompanionShowcasePublicRowPO> rows = publicQueryMapper.search(tagId, hasKeyword ? keyword : null);
        PageInfo<CompanionShowcasePublicRowPO> pageInfo = new PageInfo<>(rows);
        List<CompanionShowcasePublicSummary> summaries = rows.stream().map(this::toSummary).toList();
        return PageResult.of(summaries, pageInfo.getTotal(), pageNum, pageSize);
    }

    private CompanionShowcasePublicSummary toSummary(CompanionShowcasePublicRowPO row) {
        Set<Long> tagIds = new HashSet<>(revisionTagMapper.selectTagIdsByRevisionId(row.getRevisionId()));
        String coverPhotoObjectKey = revisionMediaMapper.selectList(
                        new LambdaQueryWrapper<CompanionShowcaseRevisionMediaPO>()
                                .eq(CompanionShowcaseRevisionMediaPO::getRevisionId, row.getRevisionId())
                                .eq(CompanionShowcaseRevisionMediaPO::getMediaType,
                                        CompanionShowcaseMediaType.PHOTO.name())
                                .orderByAsc(CompanionShowcaseRevisionMediaPO::getSortOrder)
                                .last("LIMIT 1"))
                .stream().findFirst().map(CompanionShowcaseRevisionMediaPO::getObjectKey).orElse(null);
        return new CompanionShowcasePublicSummary(row.getUserId(), row.getNickname(), row.getAvatarObjectKey(),
                row.getBio(), row.getTagline(), row.getAvailability(), tagIds, coverPhotoObjectKey,
                row.getPublishedAt());
    }

    private CompanionShowcasePO toPO(CompanionShowcase showcase) {
        CompanionShowcasePO po = new CompanionShowcasePO();
        po.setUserId(showcase.getUserId());
        po.setPublishedRevisionId(showcase.getPublishedRevisionId());
        po.setPublishedAt(showcase.getPublishedAt());
        return po;
    }

    private CompanionShowcase toDomain(CompanionShowcasePO po) {
        return CompanionShowcase.restore(po.getUserId(), po.getPublishedRevisionId(), po.getPublishedAt());
    }
}
