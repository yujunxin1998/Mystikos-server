package com.mystikos.identity.infrastructure.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 老板浏览陪玩名片目录：只看已发布内容，天然要联 identity_companion_showcase +
 * identity_companion_showcase_revision + identity_user 三张表，单表的 BaseMapper/LambdaQueryWrapper
 * 撑不住，跟 {@link CompanionShowcaseRevisionQueryMapper} 一样用注解式 SQL，不建 XML 文件
 * （本项目约定不用 XML mapper）。PageHelper 分页拦截器不区分 XML/注解/BaseMapper，
 * 调用前照常 PageHelper.startPage(...) 即可。
 */
@Mapper
public interface CompanionShowcasePublicQueryMapper {

    @Select("<script>"
            + "SELECT r.id AS revision_id, s.user_id AS user_id, u.nickname, u.avatar_object_key, "
            + "r.bio, r.tagline, r.availability, s.published_at "
            + "FROM identity_companion_showcase s "
            + "JOIN identity_companion_showcase_revision r ON r.id = s.published_revision_id "
            + "JOIN identity_user u ON u.id = s.user_id "
            + "<where>"
            + "s.published_revision_id IS NOT NULL "
            + "<if test='tagId != null'>AND EXISTS (SELECT 1 FROM identity_companion_showcase_revision_tag t "
            + "WHERE t.revision_id = r.id AND t.tag_id = #{tagId})</if> "
            + "<if test='keyword != null'>AND (u.nickname LIKE CONCAT('%',#{keyword},'%') "
            + "OR r.tagline LIKE CONCAT('%',#{keyword},'%') OR r.bio LIKE CONCAT('%',#{keyword},'%'))</if> "
            + "</where>"
            + "ORDER BY s.published_at DESC"
            + "</script>")
    List<CompanionShowcasePublicRowPO> search(@Param("tagId") Long tagId, @Param("keyword") String keyword);
}
