package com.mystikos.identity.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mystikos.common.result.PageResult;
import com.mystikos.identity.domain.model.CompanionShowcaseMediaType;
import com.mystikos.identity.domain.model.CompanionShowcaseRevision;
import com.mystikos.identity.domain.model.CompanionShowcaseRevisionStatus;
import com.mystikos.identity.domain.repository.CompanionShowcaseRevisionRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class CompanionShowcaseRevisionRepositoryImpl implements CompanionShowcaseRevisionRepository {

    private final CompanionShowcaseRevisionMapper mapper;
    private final CompanionShowcaseRevisionTagMapper tagMapper;
    private final CompanionShowcaseRevisionMediaMapper mediaMapper;
    private final CompanionShowcaseRevisionQueryMapper queryMapper;

    public CompanionShowcaseRevisionRepositoryImpl(CompanionShowcaseRevisionMapper mapper,
                                                     CompanionShowcaseRevisionTagMapper tagMapper,
                                                     CompanionShowcaseRevisionMediaMapper mediaMapper,
                                                     CompanionShowcaseRevisionQueryMapper queryMapper) {
        this.mapper = mapper;
        this.tagMapper = tagMapper;
        this.mediaMapper = mediaMapper;
        this.queryMapper = queryMapper;
    }

    @Override
    @Transactional
    public CompanionShowcaseRevision save(CompanionShowcaseRevision revision) {
        CompanionShowcaseRevisionPO po = toPO(revision);
        if (po.getId() == null) {
            mapper.insert(po);
            revision.assignId(po.getId());
        } else {
            mapper.updateById(po);
        }

        tagMapper.deleteByRevisionId(po.getId());
        for (Long tagId : revision.getTagIds()) {
            tagMapper.insert(po.getId(), tagId);
        }

        mediaMapper.delete(new LambdaQueryWrapper<CompanionShowcaseRevisionMediaPO>()
                .eq(CompanionShowcaseRevisionMediaPO::getRevisionId, po.getId()));
        insertMedia(po.getId(), CompanionShowcaseMediaType.PHOTO, revision.getPhotoObjectKeys());
        insertMedia(po.getId(), CompanionShowcaseMediaType.VIDEO, revision.getVideoObjectKeys());
        insertMedia(po.getId(), CompanionShowcaseMediaType.AUDIO, revision.getAudioObjectKeys());

        return revision;
    }

    @Override
    public Optional<CompanionShowcaseRevision> findById(Long id) {
        CompanionShowcaseRevisionPO po = mapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Optional<CompanionShowcaseRevision> findLatestByUserId(Long userId) {
        List<CompanionShowcaseRevisionPO> pos = mapper.selectList(
                new LambdaQueryWrapper<CompanionShowcaseRevisionPO>()
                        .eq(CompanionShowcaseRevisionPO::getUserId, userId)
                        .orderByDesc(CompanionShowcaseRevisionPO::getCreatedAt)
                        .last("LIMIT 1"));
        return pos.isEmpty() ? Optional.empty() : Optional.of(toDomain(pos.get(0)));
    }

    @Override
    public PageResult<CompanionShowcaseRevision> search(int pageNum, int pageSize,
                                                          CompanionShowcaseRevisionStatus status, String keyword,
                                                          OffsetDateTime createdFrom, OffsetDateTime createdTo) {
        PageHelper.startPage(pageNum, pageSize);
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        List<CompanionShowcaseRevisionRowPO> rows = queryMapper.search(
                status == null ? null : status.name(), hasKeyword ? keyword : null, createdFrom, createdTo);
        PageInfo<CompanionShowcaseRevisionRowPO> pageInfo = new PageInfo<>(rows);
        List<CompanionShowcaseRevision> revisions = rows.stream().map(this::toDomain).toList();
        return PageResult.of(revisions, pageInfo.getTotal(), pageNum, pageSize);
    }

    private void insertMedia(Long revisionId, CompanionShowcaseMediaType type, List<String> objectKeys) {
        int sortOrder = 0;
        for (String objectKey : objectKeys) {
            CompanionShowcaseRevisionMediaPO media = new CompanionShowcaseRevisionMediaPO();
            media.setRevisionId(revisionId);
            media.setMediaType(type.name());
            media.setObjectKey(objectKey);
            media.setSortOrder(sortOrder++);
            mediaMapper.insert(media);
        }
    }

    private List<String> selectMedia(Long revisionId, CompanionShowcaseMediaType type) {
        return mediaMapper.selectList(new LambdaQueryWrapper<CompanionShowcaseRevisionMediaPO>()
                        .eq(CompanionShowcaseRevisionMediaPO::getRevisionId, revisionId)
                        .eq(CompanionShowcaseRevisionMediaPO::getMediaType, type.name())
                        .orderByAsc(CompanionShowcaseRevisionMediaPO::getSortOrder))
                .stream()
                .map(CompanionShowcaseRevisionMediaPO::getObjectKey)
                .toList();
    }

    private CompanionShowcaseRevisionPO toPO(CompanionShowcaseRevision revision) {
        CompanionShowcaseRevisionPO po = new CompanionShowcaseRevisionPO();
        po.setId(revision.getId());
        po.setUserId(revision.getUserId());
        po.setBio(revision.getBio());
        po.setStatus(revision.getStatus().name());
        po.setReviewerId(revision.getReviewerId());
        po.setReviewComment(revision.getReviewComment());
        po.setReviewedAt(revision.getReviewedAt());
        po.setCreatedAt(revision.getCreatedAt());
        po.setUpdatedAt(revision.getUpdatedAt());
        return po;
    }

    private CompanionShowcaseRevision toDomain(CompanionShowcaseRevisionPO po) {
        Set<Long> tagIds = new HashSet<>(tagMapper.selectTagIdsByRevisionId(po.getId()));
        return CompanionShowcaseRevision.restore(po.getId(), po.getUserId(), po.getBio(), tagIds,
                selectMedia(po.getId(), CompanionShowcaseMediaType.PHOTO),
                selectMedia(po.getId(), CompanionShowcaseMediaType.VIDEO),
                selectMedia(po.getId(), CompanionShowcaseMediaType.AUDIO),
                CompanionShowcaseRevisionStatus.valueOf(po.getStatus()), po.getReviewerId(), po.getReviewComment(),
                po.getReviewedAt(), po.getCreatedAt(), po.getUpdatedAt());
    }

    private CompanionShowcaseRevision toDomain(CompanionShowcaseRevisionRowPO row) {
        Set<Long> tagIds = new HashSet<>(tagMapper.selectTagIdsByRevisionId(row.getId()));
        return CompanionShowcaseRevision.restore(row.getId(), row.getUserId(), row.getBio(), tagIds,
                selectMedia(row.getId(), CompanionShowcaseMediaType.PHOTO),
                selectMedia(row.getId(), CompanionShowcaseMediaType.VIDEO),
                selectMedia(row.getId(), CompanionShowcaseMediaType.AUDIO),
                CompanionShowcaseRevisionStatus.valueOf(row.getStatus()), row.getReviewerId(),
                row.getReviewComment(), row.getReviewedAt(), row.getCreatedAt(), row.getUpdatedAt());
    }
}
