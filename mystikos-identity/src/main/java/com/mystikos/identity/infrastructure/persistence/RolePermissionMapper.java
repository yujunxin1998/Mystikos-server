package com.mystikos.identity.infrastructure.persistence;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface RolePermissionMapper {

    @Select("<script>"
            + "SELECT DISTINCT permission_code FROM identity_role_permission WHERE role IN "
            + "<foreach item='r' collection='roles' open='(' separator=',' close=')'>#{r}</foreach>"
            + "</script>")
    List<String> selectPermissionCodesByRoles(@Param("roles") Collection<String> roles);

    @Insert("INSERT INTO identity_role_permission(role, permission_code) "
            + "VALUES (#{role}, #{permissionCode}) ON CONFLICT DO NOTHING")
    void grant(@Param("role") String role, @Param("permissionCode") String permissionCode);

    @Delete("DELETE FROM identity_role_permission WHERE role = #{role} AND permission_code = #{permissionCode}")
    void revoke(@Param("role") String role, @Param("permissionCode") String permissionCode);
}
