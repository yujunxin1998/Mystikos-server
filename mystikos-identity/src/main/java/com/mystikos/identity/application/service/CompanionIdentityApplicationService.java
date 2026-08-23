package com.mystikos.identity.application.service;

import com.mystikos.common.result.PageResult;
import com.mystikos.common.storage.ObjectStorageService;
import com.mystikos.identity.adapter.web.dto.SubmitCompanionApplicationRequest;
import com.mystikos.identity.domain.IdentityException;
import com.mystikos.identity.domain.model.AssessmentResult;
import com.mystikos.identity.domain.model.CompanionIdentityApplication;
import com.mystikos.identity.domain.model.CompanionIdentityApplicationStatus;
import com.mystikos.identity.domain.model.Role;
import com.mystikos.identity.domain.model.TagDefinition;
import com.mystikos.identity.domain.model.User;
import com.mystikos.identity.domain.repository.CompanionIdentityApplicationRepository;
import com.mystikos.identity.domain.repository.TagDefinitionRepository;
import com.mystikos.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 陪玩身份申请：提交、后台开始考核、录入考核结果。考核过程本身线下进行，系统只记录
 * 状态流转和最终结果，见 {@link CompanionIdentityApplication}。审核通过后自动开通陪玩身份
 * ——委托给 {@link CompanionApplicationService#grantCompanionIdentity}，跟管理员手动新增
 * 打手走同一套授予逻辑，保证台账和角色分配不会脱节。
 */
@Service
public class CompanionIdentityApplicationService {

    private final CompanionIdentityApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final TagDefinitionRepository tagDefinitionRepository;
    private final CompanionApplicationService companionApplicationService;
    private final ObjectStorageService objectStorageService;

    public CompanionIdentityApplicationService(CompanionIdentityApplicationRepository applicationRepository,
                                                UserRepository userRepository,
                                                TagDefinitionRepository tagDefinitionRepository,
                                                CompanionApplicationService companionApplicationService,
                                                ObjectStorageService objectStorageService) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.tagDefinitionRepository = tagDefinitionRepository;
        this.companionApplicationService = companionApplicationService;
        this.objectStorageService = objectStorageService;
    }

    /**
     * 提交陪玩身份申请。前置校验：已经是陪玩身份的不用再申请；已有进行中申请（SUBMITTED/
     * IN_ASSESSMENT）的不能重复提交；第三方登录账号必须先完善邮箱和手机号才能申请
     * ——申请表单自己的联系方式是独立的考核联系渠道，不能替代账号资料的完整性要求。
     */
    @Transactional
    public Long submit(Long userId, SubmitCompanionApplicationRequest request) {
        User user = getUser(userId);
        if (user.hasRole(Role.COMPANION)) {
            throw IdentityException.companionApplicationAlreadyCompanion();
        }
        if (!user.getOauthBindings().isEmpty() && (user.getEmail() == null || user.getPhone() == null)) {
            throw IdentityException.companionApplicationProfileIncomplete();
        }
        if (applicationRepository.existsActiveByUserId(userId)) {
            throw IdentityException.companionApplicationAlreadyPending();
        }

        Set<Long> requestedTags = request.getTagIds() == null ? Set.of() : request.getTagIds();
        validateTags(requestedTags);

        CompanionIdentityApplication application = CompanionIdentityApplication.submit(userId,
                request.getRealName(), request.getGender(), request.getBirthDate(), request.getSelfIntro(),
                request.getGameNickname(), request.getGameRankProofObjectKey(), requestedTags,
                request.getContactCountryCode(), request.getContactPhone(), request.getContactEmail());
        return applicationRepository.save(application).getId();
    }

    /** 管理员开始考核：仅状态流转，考核过程线下进行。 */
    @Transactional
    public void startAssessment(Long applicationId) {
        CompanionIdentityApplication application = getApplication(applicationId);
        application.startAssessment();
        applicationRepository.save(application);
    }

    /**
     * 录入考核结果。考核人（从系统用户查询）和考核结果必填，审核意见可为空。
     * 通过（PASS）时自动开通陪玩身份，标签取申请里选的擅长游戏。
     */
    @Transactional
    public void review(Long applicationId, Long reviewerId, AssessmentResult result, String comment) {
        if (reviewerId != null) {
            User reviewer = getUser(reviewerId);
            if (!reviewer.hasRole(Role.ASSESSOR) && !reviewer.hasRole(Role.ADMIN)) {
                throw IdentityException.companionApplicationReviewerNotAssessor(reviewerId);
            }
        }
        CompanionIdentityApplication application = getApplication(applicationId);
        application.review(reviewerId, result, comment);
        applicationRepository.save(application);

        if (application.getStatus() == CompanionIdentityApplicationStatus.APPROVED) {
            companionApplicationService.grantCompanionIdentity(application.getUserId(), null,
                    application.getTagIds(), null, null, null, null, null, null);
        }
    }

    /** 用户自查当前申请状态；从没申请过时返回空。 */
    public CompanionIdentityApplicationView getLatestForUser(Long userId) {
        return applicationRepository.findLatestByUserId(userId).map(this::toView).orElse(null);
    }

    public PageResult<CompanionIdentityApplicationView> list(int pageNum, int pageSize,
                                                               CompanionIdentityApplicationStatus status,
                                                               String keyword, OffsetDateTime createdFrom,
                                                               OffsetDateTime createdTo) {
        PageResult<CompanionIdentityApplication> page = applicationRepository.search(pageNum, pageSize, status,
                keyword, createdFrom, createdTo);
        List<CompanionIdentityApplicationView> views = page.records().stream().map(this::toView).toList();
        return PageResult.of(views, page.total(), page.pageNum(), page.pageSize());
    }

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

    private CompanionIdentityApplication getApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> IdentityException.companionApplicationNotFound(id));
    }

    private CompanionIdentityApplicationView toView(CompanionIdentityApplication application) {
        User applicant = getUser(application.getUserId());
        List<TagView> tags = tagDefinitionRepository.findByIds(application.getTagIds()).stream()
                .map(TagApplicationService::toView)
                .toList();
        String gameRankProofObjectKey = application.getGameRankProofObjectKey();
        String gameRankProofUrl = gameRankProofObjectKey == null ? null
                : objectStorageService.presignedDownloadUrl(gameRankProofObjectKey, Duration.ofMinutes(15));
        String reviewerNickname = application.getReviewerId() == null ? null
                : userRepository.findById(application.getReviewerId()).map(User::getNickname).orElse(null);
        return new CompanionIdentityApplicationView(application.getId(), application.getUserId(),
                applicant.getNickname(), applicant.getPhone(), applicant.getEmail(), application.getRealName(),
                application.getGender(), application.getBirthDate(), application.getSelfIntro(),
                application.getGameNickname(), gameRankProofUrl, tags, application.getContactCountryCode(),
                application.getContactPhone(), application.getContactEmail(), application.getStatus(),
                application.getReviewerId(), reviewerNickname, application.getReviewResult(),
                application.getReviewComment(), application.getReviewedAt(), application.getCreatedAt());
    }
}
