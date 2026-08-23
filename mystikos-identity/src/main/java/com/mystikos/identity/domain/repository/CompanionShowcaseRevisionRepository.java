package com.mystikos.identity.domain.repository;

import com.mystikos.common.result.PageResult;
import com.mystikos.identity.domain.model.CompanionShowcaseRevision;
import com.mystikos.identity.domain.model.CompanionShowcaseRevisionStatus;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface CompanionShowcaseRevisionRepository {

    CompanionShowcaseRevision save(CompanionShowcaseRevision revision);

    Optional<CompanionShowcaseRevision> findById(Long id);

    /** 陪玩自查/编辑用：按创建时间倒序取最新一条（同一用户可能有多条历史记录）。 */
    Optional<CompanionShowcaseRevision> findLatestByUserId(Long userId);

    /**
     * 后台审核队列：按提交时间倒序分页查询，status/keyword/createdFrom/createdTo 均为可选过滤条件；
     * keyword 匹配陪玩昵称/手机号/邮箱。
     */
    PageResult<CompanionShowcaseRevision> search(int pageNum, int pageSize, CompanionShowcaseRevisionStatus status,
                                                  String keyword, OffsetDateTime createdFrom,
                                                  OffsetDateTime createdTo);
}
