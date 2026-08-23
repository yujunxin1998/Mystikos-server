package com.mystikos.identity.domain.model;

/**
 * 标签定义（游戏类型等），后台配置、前台多选。category 是自由字符串而非固定枚举
 * ——跟权限编码同一个思路（见 {@link Role} 上的注释），分类由业务后续自行扩展，
 * 框架不预置一份"允许的分类"清单。
 */
public class TagDefinition {

    private Long id;
    private String category;
    private String label;
    private int sortOrder;
    private boolean enabled;

    private TagDefinition(Long id, String category, String label, int sortOrder, boolean enabled) {
        this.id = id;
        this.category = category;
        this.label = label;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
    }

    /** 后台新建一个标签，默认启用。 */
    public static TagDefinition create(String category, String label, int sortOrder) {
        return new TagDefinition(null, category, label, sortOrder, true);
    }

    /** 从持久化数据重建，仅供仓储实现调用。 */
    public static TagDefinition restore(Long id, String category, String label, int sortOrder, boolean enabled) {
        return new TagDefinition(id, category, label, sortOrder, enabled);
    }

    public void rename(String label, int sortOrder) {
        this.label = label;
        this.sortOrder = sortOrder;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public Long getId() {
        return id;
    }

    /** 仅供仓储实现在插入后回填生成的主键。 */
    public void assignId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public String getLabel() {
        return label;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
