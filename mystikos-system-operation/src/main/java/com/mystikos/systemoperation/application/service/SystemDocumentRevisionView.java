package com.mystikos.systemoperation.application.service;

import com.mystikos.systemoperation.domain.model.SystemDocumentRevision;

import java.time.OffsetDateTime;

public record SystemDocumentRevisionView(
        int version,
        String title,
        String content,
        String updatedBy,
        OffsetDateTime updatedAt
) {

    public static SystemDocumentRevisionView from(SystemDocumentRevision revision) {
        return new SystemDocumentRevisionView(revision.version(), revision.title(), revision.content(),
                revision.updatedBy(), revision.updatedAt());
    }
}
