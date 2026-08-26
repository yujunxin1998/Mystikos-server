package com.mystikos.systemoperation.domain.repository;

import com.mystikos.common.result.PageResult;
import com.mystikos.systemoperation.domain.model.SystemDocument;
import com.mystikos.systemoperation.domain.model.SystemDocumentRevision;

import java.util.List;
import java.util.Optional;

public interface SystemDocumentRepository {

    /** 按 code upsert；实现同时旁路追加一条修订快照到历史表。 */
    SystemDocument save(SystemDocument document);

    Optional<SystemDocument> findByCode(String code);

    List<SystemDocument> findAll();

    PageResult<SystemDocumentRevision> findRevisions(String documentCode, int pageNum, int pageSize);
}
