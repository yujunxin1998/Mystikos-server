package com.mystikos.systemoperation.domain.model;

import java.time.OffsetDateTime;

/**
 * 可由后台编辑、带修订历史的系统内容文档。{@code code} 是自由字符串而非固定枚举
 * ——跟 identity 模块 TagDefinition 的 category 同一个思路，用户协议/隐私政策只是
 * 第一批用例，业务以后要加别的配置内容（公告、帮助文档……）直接复用这张表，不用改 schema。
 * 修订历史由仓储实现在 {@code save()} 时旁路维护，聚合本身不感知历史表。
 */
public class SystemDocument {

    private Long id;
    private final String code;
    private String title;
    private String content;
    private int version;
    private String updatedBy;
    private OffsetDateTime updatedAt;

    private SystemDocument(Long id, String code, String title, String content, int version,
                            String updatedBy, OffsetDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.content = content;
        this.version = version;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    /** 新建一份系统文档，初始版本号为 1。 */
    public static SystemDocument create(String code, String title, String content, String operatorId) {
        return new SystemDocument(null, code, title, content, 1, operatorId, OffsetDateTime.now());
    }

    /** 从持久化数据重建，仅供仓储实现调用。 */
    public static SystemDocument restore(Long id, String code, String title, String content, int version,
                                          String updatedBy, OffsetDateTime updatedAt) {
        return new SystemDocument(id, code, title, content, version, updatedBy, updatedAt);
    }

    /** 更新内容并递增版本号，供仓储实现落一份修订快照。 */
    public void updateContent(String title, String content, String operatorId) {
        this.title = title;
        this.content = content;
        this.version += 1;
        this.updatedBy = operatorId;
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    /** 仅供仓储实现在插入后回填生成的主键。 */
    public void assignId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getVersion() {
        return version;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
