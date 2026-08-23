package com.mystikos.identity.application.service;

import com.mystikos.common.region.RegionQueryService;
import com.mystikos.identity.domain.IdentityException;
import com.mystikos.identity.domain.model.Gender;
import com.mystikos.identity.domain.model.Role;
import com.mystikos.identity.domain.model.TagDefinition;
import com.mystikos.identity.domain.model.User;
import com.mystikos.identity.domain.repository.PermissionRepository;
import com.mystikos.identity.domain.repository.TagDefinitionRepository;
import com.mystikos.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户全生命周期管理（角色/封禁）+ S2 老板侧资料（昵称/隐私设置/标签/地区）。
 * 注册/登录属于 S1，见 {@link AuthApplicationService}。
 */
@Service
public class UserApplicationService {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final TagDefinitionRepository tagDefinitionRepository;
    private final RegionQueryService regionQueryService;

    public UserApplicationService(UserRepository userRepository,
                                   PermissionRepository permissionRepository,
                                   TagDefinitionRepository tagDefinitionRepository,
                                   RegionQueryService regionQueryService) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.tagDefinitionRepository = tagDefinitionRepository;
        this.regionQueryService = regionQueryService;
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
        List<TagView> tags = tagDefinitionRepository.findByIds(user.getTagIds()).stream()
                .map(TagApplicationService::toView)
                .toList();
        return new UserProfileView(user.getId(), user.getPhone(), user.getEmail(), user.getNickname(),
                user.isPrivacyAnonymous(), user.getGender(), user.getAvatarUrl(), user.getBirthDate(),
                user.getBio(), user.getRegionCode(), tags, user.getRoles(), user.getMembershipTierLevel(),
                user.getMembershipTierCode(), user.getStatus());
    }

    @Transactional
    public void updateProfile(Long userId, String nickname, Gender gender, String avatarUrl,
                               LocalDate birthDate, String bio, String regionCode) {
        if (regionCode != null && !regionQueryService.exists(regionCode)) {
            throw IdentityException.regionNotFound(regionCode);
        }
        User user = getUser(userId);
        user.updateProfileDetails(nickname, gender, avatarUrl, birthDate, bio, regionCode);
        userRepository.save(user);
    }

    @Transactional
    public void updatePrivacy(Long userId, boolean anonymous) {
        User user = getUser(userId);
        user.updatePrivacy(anonymous);
        userRepository.save(user);
    }

    /** 整体覆盖用户选中的标签；每个标签都要求存在且当前启用，否则拒绝整个请求。 */
    @Transactional
    public void updateTags(Long userId, Set<Long> tagIds) {
        User user = getUser(userId);
        Set<Long> requested = tagIds == null ? Set.of() : tagIds;
        List<TagDefinition> found = tagDefinitionRepository.findByIds(requested);
        Set<Long> foundIds = new HashSet<>();
        for (TagDefinition tag : found) {
            if (!tag.isEnabled()) {
                throw IdentityException.tagDisabled(tag.getId());
            }
            foundIds.add(tag.getId());
        }
        for (Long tagId : requested) {
            if (!foundIds.contains(tagId)) {
                throw IdentityException.tagNotFound(tagId);
            }
        }
        user.updateTags(requested);
        userRepository.save(user);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> IdentityException.notFound(userId));
    }
}
