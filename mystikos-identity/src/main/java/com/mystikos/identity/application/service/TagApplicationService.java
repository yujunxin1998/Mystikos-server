package com.mystikos.identity.application.service;

import com.mystikos.identity.domain.IdentityException;
import com.mystikos.identity.domain.model.TagDefinition;
import com.mystikos.identity.domain.repository.TagDefinitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 标签目录的后台配置（游戏类型等），供 {@link UserApplicationService#updateTags}
 * 校验、供前端多选控件读取。
 */
@Service
public class TagApplicationService {

    private final TagDefinitionRepository tagDefinitionRepository;

    public TagApplicationService(TagDefinitionRepository tagDefinitionRepository) {
        this.tagDefinitionRepository = tagDefinitionRepository;
    }

    @Transactional
    public Long createTag(String category, String label, int sortOrder) {
        TagDefinition tag = TagDefinition.create(category, label, sortOrder);
        return tagDefinitionRepository.save(tag).getId();
    }

    @Transactional
    public void enableTag(Long tagId) {
        TagDefinition tag = getTag(tagId);
        tag.enable();
        tagDefinitionRepository.save(tag);
    }

    @Transactional
    public void disableTag(Long tagId) {
        TagDefinition tag = getTag(tagId);
        tag.disable();
        tagDefinitionRepository.save(tag);
    }

    public List<TagView> listTags(String category, boolean onlyEnabled) {
        return tagDefinitionRepository.findByCategory(category, onlyEnabled).stream()
                .map(TagApplicationService::toView)
                .toList();
    }

    private TagDefinition getTag(Long tagId) {
        return tagDefinitionRepository.findById(tagId)
                .orElseThrow(() -> IdentityException.tagNotFound(tagId));
    }

    static TagView toView(TagDefinition tag) {
        return new TagView(tag.getId(), tag.getCategory(), tag.getLabel(), tag.getSortOrder(), tag.isEnabled());
    }
}
