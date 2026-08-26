package com.mystikos.systemoperation.application.service;

import com.mystikos.systemoperation.domain.model.SystemDocument;

import java.time.OffsetDateTime;

public record SystemDocumentView(
        String code,
        String title,
        String content,
        int version,
        String updatedBy,
        OffsetDateTime updatedAt
) {

    public static SystemDocumentView from(SystemDocument document) {
        return new SystemDocumentView(document.getCode(), document.getTitle(), document.getContent(),
                document.getVersion(), document.getUpdatedBy(), document.getUpdatedAt());
    }
}
