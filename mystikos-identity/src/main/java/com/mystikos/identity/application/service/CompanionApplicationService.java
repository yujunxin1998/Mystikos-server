package com.mystikos.identity.application.service;

import com.mystikos.common.result.PageResult;
import com.mystikos.identity.adapter.web.dto.AdminCreateCompanionRequest;
import com.mystikos.identity.domain.IdentityException;
import com.mystikos.identity.domain.model.CompanionProfile;
import com.mystikos.identity.domain.model.CompanionStats;
import com.mystikos.identity.domain.model.CompanionStatus;
import com.mystikos.identity.domain.model.CompanionSummary;
import com.mystikos.identity.domain.model.Role;
import com.mystikos.identity.domain.model.User;
import com.mystikos.identity.domain.repository.CompanionProfileRepository;
import com.mystikos.identity.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 打手（陪玩）后台管理：新增/删除/列表检索/统计卡片。"打手"就是拥有 {@link Role#COMPANION}
 * 角色的 {@link User}，不是独立账号体系——新增打手内部复用与管理员新增用户
 * （见 {@link UserApplicationService#createUser}）一致的账号创建流程，再补一行扩展资料。
 */
@Service
public class CompanionApplicationService {

    private final CompanionProfileRepository companionProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CompanionApplicationService(CompanionProfileRepository companionProfileRepository,
                                        UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.companionProfileRepository = companionProfileRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** 新增打手：创建登录账号（角色=COMPANION）+ 打手扩展资料，一次提交、同一事务。 */
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
        User user = User.register(request.getPhone(), request.getEmail(), passwordHash, Role.COMPANION);
        if (request.getNickname() != null) {
            user.updateProfile(request.getNickname());
        }
        User saved = userRepository.save(user);

        CompanionProfile profile = CompanionProfile.create(saved.getId(), request.getLevel(),
                request.getSkillTags(), request.getHourlyRate(), request.getStatus(), request.getIdCardNo(),
                request.getBankAccountName(), request.getBankAccountNo(), request.getBankName());
        companionProfileRepository.save(profile);
    }

    /** 删除打手：只删打手扩展资料这一行，不动底下的登录账号——账号删除走用户管理（UserController）。 */
    @Transactional
    public void deleteCompanion(Long userId) {
        if (!companionProfileRepository.existsByUserId(userId)) {
            throw IdentityException.companionProfileNotFound(userId);
        }
        companionProfileRepository.deleteByUserId(userId);
    }

    public PageResult<CompanionView> listCompanions(int pageNum, int pageSize, CompanionStatus status,
                                                      String keyword, OffsetDateTime createdFrom,
                                                      OffsetDateTime createdTo) {
        PageResult<CompanionSummary> page = companionProfileRepository.search(pageNum, pageSize, status, keyword,
                createdFrom, createdTo);
        List<CompanionView> views = page.records().stream().map(CompanionApplicationService::toView).toList();
        return PageResult.of(views, page.total(), page.pageNum(), page.pageSize());
    }

    /** 顶部统计卡：总打手数/可用/忙碌/平均时薪。业绩统计（接单数/流水等）本阶段没有数据源，不算在这。 */
    public CompanionStatsView getStats() {
        CompanionStats stats = companionProfileRepository.getStats();
        return new CompanionStatsView(stats.totalCount(), stats.availableCount(), stats.busyCount(),
                stats.avgHourlyRate());
    }

    private static CompanionView toView(CompanionSummary summary) {
        return new CompanionView(summary.userId(), summary.phone(), summary.email(), summary.nickname(),
                summary.avatarUrl(), summary.status(), summary.level(), summary.skillTags(), summary.hourlyRate(),
                summary.idCardNo(), summary.bankAccountName(), summary.bankAccountNo(), summary.bankName(),
                summary.createdAt(), CompanionPerformanceView.placeholder());
    }
}
