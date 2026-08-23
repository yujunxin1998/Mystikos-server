package com.mystikos.identity.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mystikos.identity.domain.model.Gender;
import com.mystikos.identity.domain.model.OAuthBinding;
import com.mystikos.identity.domain.model.Role;
import com.mystikos.identity.domain.model.User;
import com.mystikos.identity.domain.model.UserStatus;
import com.mystikos.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final OAuthBindingMapper oauthBindingMapper;
    private final UserTagMapper userTagMapper;

    public UserRepositoryImpl(UserMapper userMapper, UserRoleMapper userRoleMapper,
                               OAuthBindingMapper oauthBindingMapper, UserTagMapper userTagMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.oauthBindingMapper = oauthBindingMapper;
        this.userTagMapper = userTagMapper;
    }

    @Override
    @Transactional
    public User save(User user) {
        UserPO po = toPO(user);
        if (po.getId() == null) {
            userMapper.insert(po);
            user.assignId(po.getId());
        } else {
            userMapper.updateById(po);
        }

        userRoleMapper.deleteByUserId(po.getId());
        for (Role role : user.getRoles()) {
            userRoleMapper.insert(po.getId(), role.getCode());
        }

        oauthBindingMapper.delete(new LambdaQueryWrapper<OAuthBindingPO>()
                .eq(OAuthBindingPO::getUserId, po.getId()));
        for (OAuthBinding binding : user.getOauthBindings()) {
            OAuthBindingPO bindingPO = new OAuthBindingPO();
            bindingPO.setUserId(po.getId());
            bindingPO.setProvider(binding.provider());
            bindingPO.setProviderUserId(binding.providerUserId());
            bindingPO.setBoundAt(binding.boundAt());
            oauthBindingMapper.insert(bindingPO);
        }

        userTagMapper.deleteByUserId(po.getId());
        for (Long tagId : user.getTagIds()) {
            userTagMapper.insert(po.getId(), tagId);
        }

        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        UserPO po = userMapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomainWithAssociations);
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        UserPO po = userMapper.selectOne(
                new LambdaQueryWrapper<UserPO>().eq(UserPO::getPhone, phone));
        return Optional.ofNullable(po).map(this::toDomainWithAssociations);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        UserPO po = userMapper.selectOne(
                new LambdaQueryWrapper<UserPO>().eq(UserPO::getEmail, email));
        return Optional.ofNullable(po).map(this::toDomainWithAssociations);
    }

    @Override
    public Optional<User> findByOAuthBinding(String provider, String providerUserId) {
        OAuthBindingPO bindingPO = oauthBindingMapper.selectOne(new LambdaQueryWrapper<OAuthBindingPO>()
                .eq(OAuthBindingPO::getProvider, provider)
                .eq(OAuthBindingPO::getProviderUserId, providerUserId));
        return bindingPO == null ? Optional.empty() : findById(bindingPO.getUserId());
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userMapper.exists(new LambdaQueryWrapper<UserPO>().eq(UserPO::getPhone, phone));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userMapper.exists(new LambdaQueryWrapper<UserPO>().eq(UserPO::getEmail, email));
    }

    private User toDomainWithAssociations(UserPO po) {
        Set<Role> roles = userRoleMapper.selectRolesByUserId(po.getId()).stream()
                .map(Role::fromCode)
                .collect(Collectors.toSet());

        List<OAuthBindingPO> bindingPOs = oauthBindingMapper.selectList(
                new LambdaQueryWrapper<OAuthBindingPO>().eq(OAuthBindingPO::getUserId, po.getId()));
        Set<OAuthBinding> bindings = bindingPOs.stream()
                .map(b -> new OAuthBinding(b.getProvider(), b.getProviderUserId(), b.getBoundAt()))
                .collect(Collectors.toCollection(HashSet::new));

        Set<Long> tagIds = new HashSet<>(userTagMapper.selectTagIdsByUserId(po.getId()));

        return User.restore(
                po.getId(),
                po.getPhone(),
                po.getEmail(),
                po.getPasswordHash(),
                po.getNickname(),
                Boolean.TRUE.equals(po.getPrivacyAnonymous()),
                po.getGender() == null ? Gender.UNDISCLOSED : Gender.valueOf(po.getGender()),
                po.getAvatarUrl(),
                po.getBirthDate(),
                po.getBio(),
                po.getRegionCode(),
                UserStatus.valueOf(po.getStatus()),
                roles,
                bindings,
                tagIds,
                po.getMembershipTierLevel(),
                po.getMembershipTierCode(),
                po.getCreatedAt());
    }

    private UserPO toPO(User user) {
        UserPO po = new UserPO();
        po.setId(user.getId());
        po.setPhone(user.getPhone());
        po.setEmail(user.getEmail());
        po.setPasswordHash(user.getPasswordHash());
        po.setNickname(user.getNickname());
        po.setPrivacyAnonymous(user.isPrivacyAnonymous());
        po.setGender(user.getGender().name());
        po.setAvatarUrl(user.getAvatarUrl());
        po.setBirthDate(user.getBirthDate());
        po.setBio(user.getBio());
        po.setRegionCode(user.getRegionCode());
        po.setStatus(user.getStatus().name());
        po.setMembershipTierLevel(user.getMembershipTierLevel());
        po.setMembershipTierCode(user.getMembershipTierCode());
        po.setCreatedAt(user.getCreatedAt());
        return po;
    }
}
