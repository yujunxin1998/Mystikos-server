package com.mystikos.identity.infrastructure.persistence;

import com.mystikos.identity.domain.model.Role;
import com.mystikos.identity.domain.repository.PermissionRepository;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class PermissionRepositoryImpl implements PermissionRepository {

    private final RolePermissionMapper mapper;

    public PermissionRepositoryImpl(RolePermissionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Set<String> findPermissionCodesByRoles(Set<Role> roles) {
        if (roles.isEmpty()) {
            return Set.of();
        }
        List<String> codes = roles.stream().map(Role::getCode).collect(Collectors.toList());
        return new HashSet<>(mapper.selectPermissionCodesByRoles(codes));
    }

    @Override
    public void grant(Role role, String permissionCode) {
        mapper.grant(role.getCode(), permissionCode);
    }

    @Override
    public void revoke(Role role, String permissionCode) {
        mapper.revoke(role.getCode(), permissionCode);
    }
}
