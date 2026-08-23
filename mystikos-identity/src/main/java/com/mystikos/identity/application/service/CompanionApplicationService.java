package com.mystikos.identity.application.service;

import com.mystikos.common.result.PageResult;
import com.mystikos.identity.adapter.web.dto.AdminCreateCompanionRequest;
import com.mystikos.identity.domain.IdentityException;
import com.mystikos.identity.domain.model.CompanionProfile;
import com.mystikos.identity.domain.model.CompanionStats;
import com.mystikos.identity.domain.model.CompanionStatus;
import com.mystikos.identity.domain.model.CompanionSummary;
import com.mystikos.identity.domain.model.Role;
import com.mystikos.identity.domain.model.TagDefinition;
import com.mystikos.identity.domain.model.User;
import com.mystikos.identity.domain.repository.CompanionProfileRepository;
import com.mystikos.identity.domain.repository.TagDefinitionRepository;
import com.mystikos.identity.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 打手（陪玩）后台管理：新增/删除/列表检索/统计卡片，以及授予/收回陪玩身份的核心逻辑。
 * "打手"就是拥有 {@link Role#COMPANION} 角色的 {@link User}，不是独立账号体系。
 * <p>
 * {@link #grantCompanionIdentity}/{@link #revokeCompanionIdentity} 是身份台账
 * （{@link CompanionProfile}）与角色分配保持同步的唯一入口——不管是这里的管理员手动
 * 新增/删除打手，还是 {@link CompanionIdentityApplicationService} 审核通过陪玩身份申请后
 * 自动开通，都必须走这两个方法，不能在别处单独改台账或单独改角色，否则两边会脱节
 * （历史上 {@code deleteCompanion} 只删台账不摘角色就是这个问题）。
 */
@Service
public class CompanionApplicationService {

    private final CompanionProfileRepository companionProfileRepository;
    private final UserRepository userRepository;
    private final TagDefinitionRepository tagDefinitionRepository;
    private final PasswordEncoder passwordEncoder;

    public CompanionApplicationService(CompanionProfileRepository companionProfileRepository,
                                        UserRepository userRepository,
                                        TagDefinitionRepository tagDefinitionRepository,
                                        PasswordEncoder passwordEncoder) {
        this.companionProfileRepository = companionProfileRepository;
        this.userRepository = userRepository;
        this.tagDefinitionRepository = tagDefinitionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** 新增打手：创建登录账号（角色 MEMBER）+ 授予陪玩身份，一次提交、同一事务。 */
    @Transactional
    public void createCompanion(AdminCreateCompanionRequest request) {
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw IdentityException.identifierAlreadyExists(request.getPhone());
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw IdentityException.identifierAlreadyExists(request.getEmail());
        }
        String passwordHash = request.getPassword() != null
                ? passwordEncoder.encode(request.getPassword())
                : null;
        User user = User.register(request.getPhone(), request.getEmail(), passwordHash, Role.MEMBER);
        if (request.getNickname() != null) {
            user.updateProfile(request.getNickname());
        }
        User saved = userRepository.save(user);

        grantCompanionIdentity(saved.getId(), request.getLevel(), request.getTagIds(), request.getHourlyRate(),
                request.getStatus(), request.getIdCardNo(), request.getBankAccountName(),
                request.getBankAccountNo(), request.getBankName());
    }

    /** 删除打手：收回陪玩身份（软删除，账号本身不受影响）。账号删除走用户管理（UserController）。 */
    @Transactional
    public void deleteCompanion(Long userId) {
        revokeCompanionIdentity(userId);
    }

    /**
     * 授予陪玩身份：确保角色里有 COMPANION，并且身份台账处于 ACTIVE。
     * 如果这个用户之前从没当过陪玩，新建一份台账（把 level/hourlyRate/idCardNo/银行信息都落进去）；
     * 如果台账已存在（不管是首次还是被收回过），只翻转身份状态+覆盖擅长游戏标签，
     * 不覆盖已有的运营资料——申请审核通过这条路径不会带 level/费率/银行信息这些数据，
     * 不能因为重新开通就把之前维护的资料清空。
     */
    @Transactional
    public void grantCompanionIdentity(Long userId, String level, Set<Long> tagIds, BigDecimal hourlyRate,
                                        CompanionStatus status, String idCardNo, String bankAccountName,
                                        String bankAccountNo, String bankName) {
        Set<Long> requestedTags = tagIds == null ? Set.of() : tagIds;
        validateTags(requestedTags);

        User user = getUser(userId);
        if (!user.hasRole(Role.COMPANION)) {
            user.assignRole(Role.COMPANION);
            userRepository.save(user);
        }

        CompanionProfile profile = companionProfileRepository.findByUserId(userId)
                .map(existing -> {
                    existing.grant(requestedTags);
                    return existing;
                })
                .orElseGet(() -> CompanionProfile.create(userId, level, requestedTags, hourlyRate, status,
                        idCardNo, bankAccountName, bankAccountNo, bankName));
        companionProfileRepository.save(profile);
    }

    /** 收回陪玩身份：台账软删除（资料保留），并摘掉 COMPANION 角色。 */
    @Transactional
    public void revokeCompanionIdentity(Long userId) {
        CompanionProfile profile = companionProfileRepository.findByUserId(userId)
                .orElseThrow(() -> IdentityException.companionProfileNotFound(userId));
        profile.revoke();
        companionProfileRepository.save(profile);

        User user = getUser(userId);
        if (user.hasRole(Role.COMPANION)) {
            user.removeRole(Role.COMPANION);
            userRepository.save(user);
        }
    }

    public PageResult<CompanionView> listCompanions(int pageNum, int pageSize, CompanionStatus status,
                                                      String keyword, OffsetDateTime createdFrom,
                                                      OffsetDateTime createdTo) {
        PageResult<CompanionSummary> page = companionProfileRepository.search(pageNum, pageSize, status, keyword,
                createdFrom, createdTo);
        List<CompanionView> views = page.records().stream().map(this::toView).toList();
        return PageResult.of(views, page.total(), page.pageNum(), page.pageSize());
    }

    /** 顶部统计卡：总打手数/可用/忙碌/平均时薪。业绩统计（接单数/流水等）本阶段没有数据源，不算在这。 */
    public CompanionStatsView getStats() {
        CompanionStats stats = companionProfileRepository.getStats();
        return new CompanionStatsView(stats.totalCount(), stats.availableCount(), stats.busyCount(),
                stats.avgHourlyRate());
    }

    /** 每个标签都要求存在且当前启用，否则拒绝整个请求，跟 {@link UserApplicationService#updateTags} 同一套校验。 */
    private void validateTags(Set<Long> tagIds) {
        List<TagDefinition> found = tagDefinitionRepository.findByIds(tagIds);
        Set<Long> foundIds = new HashSet<>();
        for (TagDefinition tag : found) {
            if (!tag.isEnabled()) {
                throw IdentityException.tagDisabled(tag.getId());
            }
            foundIds.add(tag.getId());
        }
        for (Long tagId : tagIds) {
            if (!foundIds.contains(tagId)) {
                throw IdentityException.tagNotFound(tagId);
            }
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> IdentityException.notFound(userId));
    }

    private CompanionView toView(CompanionSummary summary) {
        List<TagView> tags = tagDefinitionRepository.findByIds(summary.tagIds()).stream()
                .map(TagApplicationService::toView)
                .toList();
        return new CompanionView(summary.userId(), summary.phone(), summary.email(), summary.nickname(),
                summary.avatarUrl(), summary.status(), summary.level(), tags, summary.hourlyRate(),
                summary.idCardNo(), summary.bankAccountName(), summary.bankAccountNo(), summary.bankName(),
                summary.createdAt(), CompanionPerformanceView.placeholder());
    }
}
