package com.mystikos.identity.infrastructure.persistence;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * identity_companion_application_tag 是简单的 (application_id, tag_id) 联合主键关联表
 * （申请时选中的游戏类型），不需要独立聚合，直接用注解式 SQL，不走 BaseMapper（同 {@link UserTagMapper}）。
 */
@Mapper
public interface CompanionIdentityApplicationTagMapper {

    @Select("SELECT tag_id FROM identity_companion_application_tag WHERE application_id = #{applicationId}")
    List<Long> selectTagIdsByApplicationId(@Param("applicationId") Long applicationId);

    @Insert("INSERT INTO identity_companion_application_tag(application_id, tag_id) VALUES (#{applicationId}, #{tagId})")
    void insert(@Param("applicationId") Long applicationId, @Param("tagId") Long tagId);

    @Delete("DELETE FROM identity_companion_application_tag WHERE application_id = #{applicationId}")
    void deleteByApplicationId(@Param("applicationId") Long applicationId);
}
