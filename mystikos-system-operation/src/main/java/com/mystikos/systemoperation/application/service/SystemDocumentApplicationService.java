package com.mystikos.systemoperation.application.service;

import com.mystikos.common.result.PageResult;
import com.mystikos.systemoperation.domain.SystemOperationException;
import com.mystikos.systemoperation.domain.model.SystemDocument;
import com.mystikos.systemoperation.domain.model.SystemDocumentRevision;
import com.mystikos.systemoperation.domain.repository.SystemDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemDocumentApplicationService {

    private final SystemDocumentRepository systemDocumentRepository;

    public SystemDocumentApplicationService(SystemDocumentRepository systemDocumentRepository) {
        this.systemDocumentRepository = systemDocumentRepository;
    }

    /** 后台维护入口：文档不存在则新建（版本从1开始），存在则更新内容并把版本号加一。 */
    @Transactional
    public SystemDocumentView createOrUpdate(String code, String title, String content, String operatorId) {
        SystemDocument document = systemDocumentRepository.findByCode(code).orElse(null);
        if (document == null) {
            document = SystemDocument.create(code, title, content, operatorId);
        } else {
            document.updateContent(title, content, operatorId);
        }
        return SystemDocumentView.from(systemDocumentRepository.save(document));
    }

    public SystemDocumentView get(String code) {
        return systemDocumentRepository.findByCode(code)
                .map(SystemDocumentView::from)
                .orElseThrow(() -> SystemOperationException.documentNotFound(code));
    }

    public List<SystemDocumentView> listAll() {
        return systemDocumentRepository.findAll().stream().map(SystemDocumentView::from).toList();
    }

    public PageResult<SystemDocumentRevisionView> listRevisions(String code, int pageNum, int pageSize) {
        PageResult<SystemDocumentRevision> page = systemDocumentRepository.findRevisions(code, pageNum, pageSize);
        List<SystemDocumentRevisionView> views =
                page.records().stream().map(SystemDocumentRevisionView::from).toList();
        return PageResult.of(views, page.total(), page.pageNum(), page.pageSize());
    }
}
