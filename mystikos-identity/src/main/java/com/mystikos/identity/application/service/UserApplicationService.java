package com.mystikos.identity.application.service;

import com.mystikos.identity.domain.IdentityException;
import com.mystikos.identity.domain.model.Role;
import com.mystikos.identity.domain.model.User;
import com.mystikos.identity.domain.repository.PermissionRepository;
import com.mystikos.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * 用户全生命周期管理（角色/封禁）+ S2 老板侧资料（昵称/隐私设置）。
 * 注册/登录属于 S1，见 {@link AuthApplicationService}。
 */
@Service
public class UserApplicationService {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    public UserApplicationService(UserRepository userRepository,
                                   PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    public void assignRole(Long userId, Role role) {
        User user = getUser(userId);
        user.assignRole(role);
        userRepository.save(user);
    }

    @Transactional
    public void removeRole(Long userId, Role role) {
        User user = getUser(userId);
        user.removeRole(role);
        userRepository.save(user);
    }

    @Transactional
    public void ban(Long userId) {
        User user = getUser(userId);
        user.ban();
        userRepository.save(user);
    }

    /** ADMIN 角色隐含拥有全部权限，用通配符 "*" 表示，不依赖 role_permission 表配置。 */
    public Set<String> resolvePermissions(Long userId) {
        User user = getUser(userId);
        if (user.hasRole(Role.ADMIN)) {
            return Set.of("*");
        }
        return permissionRepository.findPermissionCodesByRoles(user.getRoles());
    }

    public UserProfileView getProfile(Long userId) {
        User user = getUser(userId);
        return new UserProfileView(user.getId(), user.getPhone(), user.getEmail(), user.getNickname(),
                user.isPrivacyAnonymous(), user.getRoles(), user.getMembershipTierLevel(),
                user.getMembershipTierCode(), user.getStatus());
    }

    @Transactional
    public void updateProfile(Long userId, String nickname) {
        User user = getUser(userId);
        user.updateProfile(nickname);
        userRepository.save(user);
    }

    @Transactional
    public void updatePrivacy(Long userId, boolean anonymous) {
        User user = getUser(userId);
        user.updatePrivacy(anonymous);
        userRepository.save(user);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> IdentityException.notFound(userId));
    }
}
