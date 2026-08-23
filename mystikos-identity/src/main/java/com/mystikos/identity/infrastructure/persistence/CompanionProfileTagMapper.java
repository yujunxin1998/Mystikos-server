package com.mystikos.identity.infrastructure.persistence;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * identity_companion_profile_tag 是简单的 (user_id, tag_id) 联合主键关联表（陪玩擅长的
 * 游戏类型），不需要独立聚合，直接用注解式 SQL，不走 BaseMapper（同 {@link UserTagMapper}）。
 */
@Mapper
public interface CompanionProfileTagMapper {

    @Select("SELECT tag_id FROM identity_companion_profile_tag WHERE user_id = #{userId}")
    List<Long> selectTagIdsByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO identity_companion_profile_tag(user_id, tag_id) VALUES (#{userId}, #{tagId})")
    void insert(@Param("userId") Long userId, @Param("tagId") Long tagId);

    @Delete("DELETE FROM identity_companion_profile_tag WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Long userId);
}
