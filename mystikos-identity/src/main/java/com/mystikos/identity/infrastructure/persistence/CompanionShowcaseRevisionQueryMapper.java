package com.mystikos.identity.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 后台审核队列按关键字（陪玩昵称/手机号/邮箱）搜索，天然要联 identity_user 表，单表的
 * BaseMapper/LambdaQueryWrapper 撑不住，跟 {@link CompanionQueryMapper} 一样用注解式 SQL，
 * 不建 XML 文件（本项目约定不用 XML mapper）。查询结果映射到 {@link CompanionShowcaseRevisionRowPO}
 * 这个纯查询行对象，不是 MyBatis-Plus 实体。PageHelper 分页拦截器不区分 XML/注解/BaseMapper，
 * 调用前照常 PageHelper.startPage(...) 即可。
 */
@Mapper
public interface CompanionShowcaseRevisionQueryMapper {

    @Select("<script>"
            + "SELECT r.* FROM identity_companion_showcase_revision r JOIN identity_user u ON u.id = r.user_id "
            + "<where>"
            + "<if test='status != null'>AND r.status = #{status}</if> "
            + "<if test='keyword != null'>AND (u.nickname LIKE CONCAT('%',#{keyword},'%') "
            + "OR u.phone LIKE CONCAT('%',#{keyword},'%') OR u.email LIKE CONCAT('%',#{keyword},'%'))</if> "
            + "<if test='createdFrom != null'>AND r.created_at &gt;= #{createdFrom}</if> "
            + "<if test='createdTo != null'>AND r.created_at &lt;= #{createdTo}</if> "
            + "</where>"
            + "ORDER BY r.created_at DESC"
            + "</script>")
    List<CompanionShowcaseRevisionRowPO> search(@Param("status") String status, @Param("keyword") String keyword,
                                                 @Param("createdFrom") OffsetDateTime createdFrom,
                                                 @Param("createdTo") OffsetDateTime createdTo);
}
