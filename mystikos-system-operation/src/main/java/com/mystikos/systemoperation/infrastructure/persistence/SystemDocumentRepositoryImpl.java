package com.mystikos.systemoperation.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mystikos.common.result.PageResult;
import com.mystikos.systemoperation.domain.model.SystemDocument;
import com.mystikos.systemoperation.domain.model.SystemDocumentRevision;
import com.mystikos.systemoperation.domain.repository.SystemDocumentRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * {@code save()} 按 code upsert 当前内容，同时旁路追加一条修订快照到
 * {@code sysop_document_revision}——两张表在同一个事务里维护，domain 层不感知历史表。
 */
@Repository
public class SystemDocumentRepositoryImpl implements SystemDocumentRepository {

    private final SystemDocumentMapper mapper;
    private final SystemDocumentRevisionMapper revisionMapper;

    public SystemDocumentRepositoryImpl(SystemDocumentMapper mapper, SystemDocumentRevisionMapper revisionMapper) {
        this.mapper = mapper;
        this.revisionMapper = revisionMapper;
    }

    @Override
    @Transactional
    public SystemDocument save(SystemDocument document) {
        SystemDocumentPO po = toPO(document);
        if (po.getId() == null) {
            mapper.insert(po);
            document.assignId(po.getId());
        } else {
            mapper.updateById(po);
        }
        revisionMapper.insert(toRevisionPO(document));
        return document;
    }

    @Override
    public Optional<SystemDocument> findByCode(String code) {
        SystemDocumentPO po = mapper.selectOne(
                new LambdaQueryWrapper<SystemDocumentPO>().eq(SystemDocumentPO::getCode, code));
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<SystemDocument> findAll() {
        return mapper.selectList(new LambdaQueryWrapper<SystemDocumentPO>()
                        .orderByAsc(SystemDocumentPO::getCode))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public PageResult<SystemDocumentRevision> findRevisions(String documentCode, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<SystemDocumentRevisionPO> rows = revisionMapper.selectList(
                new LambdaQueryWrapper<SystemDocumentRevisionPO>()
                        .eq(SystemDocumentRevisionPO::getDocumentCode, documentCode)
                        .orderByDesc(SystemDocumentRevisionPO::getVersion));
        PageInfo<SystemDocumentRevisionPO> pageInfo = new PageInfo<>(rows);
        List<SystemDocumentRevision> revisions = rows.stream().map(this::toRevisionDomain).toList();
        return PageResult.of(revisions, pageInfo.getTotal(), pageNum, pageSize);
    }

    private SystemDocumentRevision toRevisionDomain(SystemDocumentRevisionPO po) {
        return new SystemDocumentRevision(po.getVersion(), po.getTitle(), po.getContent(),
                po.getUpdatedBy(), po.getCreatedAt());
    }

    private SystemDocumentPO toPO(SystemDocument document) {
        SystemDocumentPO po = new SystemDocumentPO();
        po.setId(document.getId());
        po.setCode(document.getCode());
        po.setTitle(document.getTitle());
        po.setContent(document.getContent());
        po.setVersion(document.getVersion());
        po.setUpdatedBy(document.getUpdatedBy());
        po.setUpdatedAt(document.getUpdatedAt());
        return po;
    }

    private SystemDocumentRevisionPO toRevisionPO(SystemDocument document) {
        SystemDocumentRevisionPO revision = new SystemDocumentRevisionPO();
        revision.setDocumentCode(document.getCode());
        revision.setVersion(document.getVersion());
        revision.setTitle(document.getTitle());
        revision.setContent(document.getContent());
        revision.setUpdatedBy(document.getUpdatedBy());
        revision.setCreatedAt(document.getUpdatedAt());
        return revision;
    }

    private SystemDocument toDomain(SystemDocumentPO po) {
        return SystemDocument.restore(po.getId(), po.getCode(), po.getTitle(), po.getContent(),
                po.getVersion(), po.getUpdatedBy(), po.getUpdatedAt());
    }
}
