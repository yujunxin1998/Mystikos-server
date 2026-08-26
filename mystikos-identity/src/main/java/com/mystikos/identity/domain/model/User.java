package com.mystikos.identity.domain.model;

import com.mystikos.common.membership.MembershipTier;
import com.mystikos.identity.domain.IdentityException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户聚合根。手机号和邮箱是登录标识（二选一即可，不强制都填），
 * 不再有独立的 username 字段。角色是固定枚举集合（{@link Role}），
 * 会员等级只存 level/code 两个字段，具体梯度由 {@link MembershipTier}
 * 的某个实现给出，本聚合不关心梯度长什么样。
 */
public class User {

    private Long id;
    private String phone;
    private String email;
    private String passwordHash;
    private String nickname;
    private boolean privacyAnonymous;
    private Gender gender;
    private String avatarObjectKey;
    private LocalDate birthDate;
    private String bio;
    private String regionCode;
    private UserStatus status;
    private final Set<Role> roles;
    private final Set<OAuthBinding> oauthBindings;
    private final Set<Long> tagIds;
    private Integer membershipTierLevel;
    private String membershipTierCode;
    private final OffsetDateTime createdAt;

    private User(Long id, String phone, String email, String passwordHash, String nickname,
                  boolean privacyAnonymous, Gender gender, String avatarObjectKey, LocalDate birthDate,
                  String bio, String regionCode, UserStatus status, Set<Role> roles,
                  Set<OAuthBinding> oauthBindings, Set<Long> tagIds, Integer membershipTierLevel,
                  String membershipTierCode, OffsetDateTime createdAt) {
        boolean hasOAuth = oauthBindings != null && !oauthBindings.isEmpty();
        if (phone == null && email == null && !hasOAuth) {
            throw IdentityException.identifierRequired();
        }
        this.id = id;
        this.phone = phone;
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.privacyAnonymous = privacyAnonymous;
        this.gender = gender == null ? Gender.UNDISCLOSED : gender;
        this.avatarObjectKey = avatarObjectKey;
        this.birthDate = birthDate;
        this.bio = bio;
        this.regionCode = regionCode;
        this.status = status;
        this.roles = roles.isEmpty() ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(roles);
        this.oauthBindings = hasOAuth ? new HashSet<>(oauthBindings) : new HashSet<>();
        this.tagIds = tagIds == null ? new HashSet<>() : new HashSet<>(tagIds);
        this.membershipTierLevel = membershipTierLevel;
        this.membershipTierCode = membershipTierCode;
        this.createdAt = createdAt;
    }

    /** 手机号或邮箱注册（至少给一个），初始状态 ACTIVE。 */
    public static User register(String phone, String email, String passwordHash, Role initialRole) {
        return new User(null, phone, email, passwordHash, null, false, Gender.UNDISCLOSED, null, null,
                null, null, UserStatus.ACTIVE, EnumSet.of(initialRole), Set.of(), Set.of(), null, null,
                OffsetDateTime.now());
    }

    /** 第三方登录首次授权、本地找不到对应账号时自动注册。 */
    public static User registerWithOAuth(OAuthBinding binding, Role initialRole) {
        return new User(null, null, null, null, null, false, Gender.UNDISCLOSED, null, null,
                null, null, UserStatus.ACTIVE, EnumSet.of(initialRole), Set.of(binding), Set.of(), null, null,
                OffsetDateTime.now());
    }

    /** 从持久化数据重建聚合，仅供仓储实现调用。 */
    public static User restore(Long id, String phone, String email, String passwordHash, String nickname,
                                boolean privacyAnonymous, Gender gender, String avatarObjectKey, LocalDate birthDate,
                                String bio, String regionCode, UserStatus status, Set<Role> roles,
                                Set<OAuthBinding> oauthBindings, Set<Long> tagIds, Integer membershipTierLevel,
                                String membershipTierCode, OffsetDateTime createdAt) {
        return new User(id, phone, email, passwordHash, nickname, privacyAnonymous, gender, avatarObjectKey,
                birthDate, bio, regionCode, status, roles, oauthBindings, tagIds, membershipTierLevel,
                membershipTierCode, createdAt);
    }

    /** 绑定一个第三方身份；同一 provider 再次绑定视为更新（比如换绑）。 */
    public void bindOAuth(OAuthBinding binding) {
        oauthBindings.removeIf(existing -> existing.provider().equals(binding.provider()));
        oauthBindings.add(binding);
    }

    /**
     * 解绑一个第三方身份。解绑后必须仍满足"手机号/邮箱/第三方绑定至少一项"的不变量
     * （与构造函数的校验一致），否则拒绝——不允许把账号解到无法登录的状态。
     * 验证码校验等前置确认由应用服务负责，本方法只管领域不变量。
     */
    public void unbindOAuth(String provider) {
        OAuthBinding existing = oauthBindings.stream()
                .filter(binding -> binding.provider().equals(provider))
                .findFirst()
                .orElseThrow(() -> IdentityException.oauthBindingNotFound(provider));
        boolean hasOtherIdentity = phone != null || email != null || oauthBindings.size() > 1;
        if (!hasOtherIdentity) {
            throw IdentityException.oauthUnbindRequiresOtherLoginMethod();
        }
        oauthBindings.remove(existing);
    }

    public void updateProfile(String nickname) {
        this.nickname = nickname;
    }

    /** 绑定经过验证码校验的登录标识。验证码消费和唯一性检查由应用服务负责。 */
    public void bindContact(AuthChannel channel, String identifier) {
        if (channel == AuthChannel.PHONE) {
            this.phone = identifier;
        } else {
            this.email = identifier;
        }
    }

    /**
     * 老板资料的完整编辑入口（昵称之外的性别/头像/生日/签名/地区）。
     * birthDate 只是自报展示信息，不作为年龄/未成年人判定依据——那个判定属于
     * 独立的实名认证流程（见 docs/architecture/prd-alignment.md 缺口2），
     * 两者故意不互相赋值。
     */
    public void updateProfileDetails(String nickname, Gender gender, String avatarObjectKey, LocalDate birthDate,
                                      String bio, String regionCode) {
        this.nickname = nickname;
        this.gender = gender == null ? Gender.UNDISCLOSED : gender;
        this.avatarObjectKey = avatarObjectKey;
        this.birthDate = birthDate;
        this.bio = bio;
        this.regionCode = regionCode;
    }

    public void updatePrivacy(boolean anonymous) {
        this.privacyAnonymous = anonymous;
    }

    /**
     * 整体覆盖当前用户选中的标签（游戏类型等，多选）。合法性校验（标签存在且启用）
     * 由应用服务在调用前完成——聚合本身不认识 {@link TagDefinition} 仓储。
     */
    public void updateTags(Set<Long> tagIds) {
        this.tagIds.clear();
        if (tagIds != null) {
            this.tagIds.addAll(tagIds);
        }
    }

    public void assignRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        if (!roles.contains(role)) {
            throw IdentityException.roleNotAssigned(role);
        }
        if (roles.size() == 1) {
            throw IdentityException.lastRoleCannotBeRemoved(role);
        }
        roles.remove(role);
        if (role == Role.MEMBER) {
            membershipTierLevel = null;
            membershipTierCode = null;
        }
    }

    /** 设置会员等级；调用方传入的是业务自己定义的 {@link MembershipTier} 实现，本方法不关心具体梯度。 */
    public void updateMembershipTier(MembershipTier tier) {
        if (!roles.contains(Role.MEMBER)) {
            throw IdentityException.notMember(id);
        }
        this.membershipTierLevel = tier.getLevel();
        this.membershipTierCode = tier.getCode();
    }

    public void ban() {
        status = UserStatus.BANNED;
    }

    public void enable() {
        status = UserStatus.ACTIVE;
    }

    public void disable() {
        status = UserStatus.DISABLED;
    }

    /** 管理员删除用户：逻辑删除，账号状态置为 DELETED，不物理删行。 */
    public void delete() {
        status = UserStatus.DELETED;
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public Long getId() {
        return id;
    }

    /** 仅供仓储实现在插入后回填生成的主键。 */
    public void assignId(Long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isPrivacyAnonymous() {
        return privacyAnonymous;
    }

    public Gender getGender() {
        return gender;
    }

    public String getAvatarObjectKey() {
        return avatarObjectKey;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getBio() {
        return bio;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public Set<OAuthBinding> getOauthBindings() {
        return Collections.unmodifiableSet(oauthBindings);
    }

    public Set<Long> getTagIds() {
        return Collections.unmodifiableSet(tagIds);
    }

    public Integer getMembershipTierLevel() {
        return membershipTierLevel;
    }

    public String getMembershipTierCode() {
        return membershipTierCode;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
