package com.mystikos.identity.domain.repository;

import com.mystikos.common.result.PageResult;
import com.mystikos.identity.domain.model.CompanionShowcase;
import com.mystikos.identity.domain.model.CompanionShowcasePublicSummary;

import java.util.Optional;

public interface CompanionShowcaseRepository {

    CompanionShowcase save(CompanionShowcase showcase);

    Optional<CompanionShowcase> findByUserId(Long userId);

    /** 老板浏览目录：只看已发布内容，按发布时间倒序，支持按游戏标签/关键字（昵称/一句话标签/简介）过滤。 */
    PageResult<CompanionShowcasePublicSummary> searchPublished(int pageNum, int pageSize, Long tagId, String keyword);
}
