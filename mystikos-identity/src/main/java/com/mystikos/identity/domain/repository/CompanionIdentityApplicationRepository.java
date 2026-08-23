package com.mystikos.identity.domain.repository;

import com.mystikos.common.result.PageResult;
import com.mystikos.identity.domain.model.CompanionIdentityApplication;
import com.mystikos.identity.domain.model.CompanionIdentityApplicationStatus;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface CompanionIdentityApplicationRepository {

    CompanionIdentityApplication save(CompanionIdentityApplication application);

    Optional<CompanionIdentityApplication> findById(Long id);

    /** 用户自查当前申请状态用：按提交时间倒序取最新一条（同一用户可能有多条历史申请）。 */
    Optional<CompanionIdentityApplication> findLatestByUserId(Long userId);

    /** 是否存在未走完流程的申请（SUBMITTED/IN_ASSESSMENT），提交新申请前用于拦重复提交。 */
    boolean existsActiveByUserId(Long userId);

    /**
     * 后台管理列表：按提交时间倒序分页查询，status/keyword/createdFrom/createdTo 均为可选过滤条件；
     * keyword 匹配真实姓名/游戏昵称/联系手机号/联系邮箱。
     */
    PageResult<CompanionIdentityApplication> search(int pageNum, int pageSize,
                                                      CompanionIdentityApplicationStatus status, String keyword,
                                                      OffsetDateTime createdFrom, OffsetDateTime createdTo);
}
