package com.mystikos.systemoperation.domain.model;

import java.time.OffsetDateTime;

/** 一条系统文档修订快照，只读，供后台查看历史用。 */
public record SystemDocumentRevision(
        int version,
        String title,
        String content,
        String updatedBy,
        OffsetDateTime updatedAt
) {
}
