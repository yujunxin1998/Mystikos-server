package com.mystikos.identity.infrastructure.persistence;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * identity_companion_showcase_revision_tag 是简单的 (revision_id, tag_id) 联合主键关联表
 * （名片选中的游戏类型），不需要独立聚合，直接用注解式 SQL，同
 * {@link CompanionIdentityApplicationTagMapper}。
 */
@Mapper
public interface CompanionShowcaseRevisionTagMapper {

    @Select("SELECT tag_id FROM identity_companion_showcase_revision_tag WHERE revision_id = #{revisionId}")
    List<Long> selectTagIdsByRevisionId(@Param("revisionId") Long revisionId);

    @Insert("INSERT INTO identity_companion_showcase_revision_tag(revision_id, tag_id) VALUES (#{revisionId}, #{tagId})")
    void insert(@Param("revisionId") Long revisionId, @Param("tagId") Long tagId);

    @Delete("DELETE FROM identity_companion_showcase_revision_tag WHERE revision_id = #{revisionId}")
    void deleteByRevisionId(@Param("revisionId") Long revisionId);
}
