package com.mystikos.identity.domain.repository;

import com.mystikos.common.result.PageResult;
import com.mystikos.identity.domain.model.CompanionProfile;
import com.mystikos.identity.domain.model.CompanionStats;
import com.mystikos.identity.domain.model.CompanionStatus;
import com.mystikos.identity.domain.model.CompanionSummary;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface CompanionProfileRepository {

    CompanionProfile save(CompanionProfile profile);

    Optional<CompanionProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    void deleteByUserId(Long userId);

    /**
     * 后台管理列表：identity_user + identity_companion_profile 联表分页查询，按注册时间倒序。
     * status/keyword/createdFrom/createdTo 均为可选过滤条件；keyword 匹配昵称/手机号/身份证号。
     */
    PageResult<CompanionSummary> search(int pageNum, int pageSize, CompanionStatus status, String keyword,
                                         OffsetDateTime createdFrom, OffsetDateTime createdTo);

    /** 全量统计，不受任何过滤条件影响，供后台管理页顶部统计卡用。 */
    CompanionStats getStats();
}
