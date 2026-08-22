package com.mystikos.identity.infrastructure.persistence;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * identity_user_role 是简单的 (user_id, role) 联合主键关联表，
 * 不需要独立聚合，直接用注解式 SQL，不走 BaseMapper。
 */
@Mapper
public interface UserRoleMapper {

    @Select("SELECT role FROM identity_user_role WHERE user_id = #{userId}")
    List<String> selectRolesByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO identity_user_role(user_id, role) VALUES (#{userId}, #{role})")
    void insert(@Param("userId") Long userId, @Param("role") String role);

    @Delete("DELETE FROM identity_user_role WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Long userId);
}
