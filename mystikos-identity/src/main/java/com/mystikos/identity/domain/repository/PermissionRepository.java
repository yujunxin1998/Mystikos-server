package com.mystikos.identity.domain.repository;

import com.mystikos.identity.domain.model.Role;

import java.util.Set;

/**
 * 角色-权限映射的仓储接口。权限编码是纯字符串，不做成独立聚合——
 * 业务还没定义具体权限编码，不需要一个带生命周期的 Permission 实体。
 */
public interface PermissionRepository {

    Set<String> findPermissionCodesByRoles(Set<Role> roles);

    void grant(Role role, String permissionCode);

    void revoke(Role role, String permissionCode);
}
