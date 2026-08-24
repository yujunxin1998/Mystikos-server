package com.mystikos.identity.application.service;

import com.mystikos.common.result.PageResult;
import com.mystikos.common.storage.ObjectStorageService;
import com.mystikos.identity.domain.IdentityException;
import com.mystikos.identity.domain.model.CompanionShowcase;
import com.mystikos.identity.domain.model.CompanionShowcaseRevision;
import com.mystikos.identity.domain.model.CompanionShowcasePublicSummary;
import com.mystikos.identity.domain.model.CompanionShowcaseRevisionStatus;
import com.mystikos.identity.domain.model.Role;
import com.mystikos.identity.domain.model.TagDefinition;
import com.mystikos.identity.domain.model.User;
import com.mystikos.identity.domain.repository.CompanionShowcaseRepository;
import com.mystikos.identity.domain.repository.CompanionShowcaseRevisionRepository;
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
 * 陪玩名片：陪玩自己维护草稿、提交审核；管理员审核；老板端读取已发布内容。
 * 草稿/提交记录（{@link CompanionShowcaseRevision}）和已发布台账（{@link CompanionShowcase}）
 * 的关系见两个类各自的类注释——审核通过时把台账指针切到对应 revision，是本服务
 * {@link #review} 里唯一改台账的地方。
 */
@Service
public class CompanionShowcaseApplicationService {

    private static final int MAX_PHOTOS = 9;
    private static final int MAX_VIDEOS = 5;
    private static final int MAX_AUDIOS = 3;
    private static final Duration MEDIA_URL_TTL = Duration.ofMinutes(15);

    private final CompanionShowcaseRevisionRepository revisionRepository;
    private final CompanionShowcaseRepository showcaseRepository;
    private final UserRepository userRepository;
    private final TagDefinitionRepository tagDefinitionRepository;
    private final ObjectStorageService objectStorageService;

    public CompanionShowcaseApplicationService(CompanionShowcaseRevisionRepository revisionRepository,
                                                CompanionShowcaseRepository showcaseRepository,
                                                UserRepository userRepository,
                                                TagDefinitionRepository tagDefinitionRepository,
                                                ObjectStorageService objectStorageService) {
        this.revisionRepository = revisionRepository;
        this.showcaseRepository = showcaseRepository;
        this.userRepository = userRepository;
        this.tagDefinitionRepository = tagDefinitionRepository;
        this.objectStorageService = objectStorageService;
    }

    /**
     * 保存草稿：最新记录还是编辑中（DRAFT）就地更新；已经是终态（APPROVED/REJECTED）或从没编辑过
     * 就新开一条；待审核（PENDING_REVIEW）不允许编辑，要等审核结果出来。
     */
    @Transactional
    public void saveDraft(Long userId, String bio, String tagline, String availability, Set<Long> tagIds,
                           String coverObjectKey,
                           List<String> photoObjectKeys, List<String> videoObjectKeys,
                           List<String> audioObjectKeys) {
        requireCompanion(userId);
        Set<Long> requestedTags = tagIds == null ? Set.of() : tagIds;
        validateTags(requestedTags);
        validateMediaLimit("照片", photoObjectKeys, MAX_PHOTOS);
        validateMediaLimit("视频", videoObjectKeys, MAX_VIDEOS);
        validateMediaLimit("语音", audioObjectKeys, MAX_AUDIOS);

        CompanionShowcaseRevision latest = revisionRepository.findLatestByUserId(userId).orElse(null);
        CompanionShowcaseRevision revision;
        if (latest == null || latest.getStatus() != CompanionShowcaseRevisionStatus.DRAFT) {
            if (latest != null && latest.getStatus() == CompanionShowcaseRevisionStatus.PENDING_REVIEW) {
                throw IdentityException.companionShowcaseInvalidStatusTransition(latest.getStatus());
            }
            revision = CompanionShowcaseRevision.draft(userId);
        } else {
            revision = latest;
        }
        revision.updateContent(bio, tagline, availability, requestedTags, coverObjectKey, photoObjectKeys, videoObjectKeys,
                audioObjectKeys);
        revisionRepository.save(revision);
    }

    /** 提交审核：只能提交当前编辑中的草稿。 */
    @Transactional
    public void submit(Long userId) {
        requireCompanion(userId);
        CompanionShowcaseRevision revision = revisionRepository.findLatestByUserId(userId)
                .filter(r -> r.getStatus() == CompanionShowcaseRevisionStatus.DRAFT)
                .orElseThrow(() -> IdentityException.companionShowcaseRevisionNotFound(userId));
        revision.submit();
        revisionRepository.save(revision);
    }

    /**
     * 审核。通过时把已发布台账的指针切到这条 revision（{@link CompanionShowcase#publish}），
     * 老板端从此刻起就能看到新内容；不通过则只记录驳回原因，已发布内容不受影响。
     */
    @Transactional
    public void review(Long revisionId, Long reviewerId, boolean approved, String comment) {
        CompanionShowcaseRevision revision = revisionRepository.findById(revisionId)
                .orElseThrow(() -> IdentityException.companionShowcaseRevisionNotFound(revisionId));
        if (approved) {
            revision.approve(reviewerId, comment);
            revisionRepository.save(revision);

            CompanionShowcase showcase = showcaseRepository.findByUserId(revision.getUserId())
                    .orElseGet(() -> CompanionShowcase.empty(revision.getUserId()));
            showcase.publish(revision.getId());
            showcaseRepository.save(showcase);
        } else {
            revision.reject(reviewerId, comment);
            revisionRepository.save(revision);
        }
    }

    /** 陪玩自查：最新一条草稿/提交记录，没编辑过时返回空。 */
    public CompanionShowcaseView getMyDraft(Long userId) {
        return revisionRepository.findLatestByUserId(userId).map(this::toView).orElse(null);
    }

    public PageResult<CompanionShowcaseView> list(int pageNum, int pageSize, CompanionShowcaseRevisionStatus status,
                                                    String keyword, OffsetDateTime createdFrom,
                                                    OffsetDateTime createdTo) {
        PageResult<CompanionShowcaseRevision> page = revisionRepository.search(pageNum, pageSize, status, keyword,
                createdFrom, createdTo);
        List<CompanionShowcaseView> views = page.records().stream().map(this::toView).toList();
        return PageResult.of(views, page.total(), page.pageNum(), page.pageSize());
    }

    /** 老板端读取已发布名片，没发布过时报错，见 {@link IdentityException#companionShowcaseNotPublished}。 */
    public CompanionShowcasePublicView getPublished(Long userId) {
        CompanionShowcase showcase = showcaseRepository.findByUserId(userId)
                .filter(CompanionShowcase::isPublished)
                .orElseThrow(() -> IdentityException.companionShowcaseNotPublished(userId));
        CompanionShowcaseRevision revision = revisionRepository.findById(showcase.getPublishedRevisionId())
                .orElseThrow(() -> IdentityException.companionShowcaseRevisionNotFound(showcase.getPublishedRevisionId()));
        User user = getUser(userId);
        List<TagView> tags = tagDefinitionRepository.findByIds(revision.getTagIds()).stream()
                .map(TagApplicationService::toView)
                .toList();
        String avatarUrl = user.getAvatarObjectKey() == null ? null
                : objectStorageService.presignedDownloadUrl(user.getAvatarObjectKey(), MEDIA_URL_TTL);
        String coverUrl = revision.getCoverObjectKey() == null ? null : objectStorageService.presignedDownloadUrl(revision.getCoverObjectKey(), MEDIA_URL_TTL);
        return new CompanionShowcasePublicView(userId, user.getNickname(), avatarUrl, revision.getBio(),
                revision.getTagline(), revision.getAvailability(), coverUrl, tags,
                toUrls(revision.getPhotoObjectKeys()), toUrls(revision.getVideoObjectKeys()),
                toUrls(revision.getAudioObjectKeys()), showcase.getPublishedAt());
    }

    @Transactional
    public void reorderPublishedMedia(Long userId, List<String> photoObjectKeys, List<String> videoObjectKeys,
                                      List<String> audioObjectKeys) {
        requireCompanion(userId);
        CompanionShowcase showcase = showcaseRepository.findByUserId(userId)
                .filter(CompanionShowcase::isPublished)
                .orElseThrow(() -> IdentityException.companionShowcaseNotPublished(userId));
        CompanionShowcaseRevision published = revisionRepository.findById(showcase.getPublishedRevisionId())
                .orElseThrow(() -> IdentityException.companionShowcaseRevisionNotFound(showcase.getPublishedRevisionId()));
        if (!sameItems(published.getPhotoObjectKeys(), photoObjectKeys)
                || !sameItems(published.getVideoObjectKeys(), videoObjectKeys)
                || !sameItems(published.getAudioObjectKeys(), audioObjectKeys)) {
            throw IdentityException.companionShowcaseMediaOrderInvalid();
        }
        revisionRepository.reorderMedia(published.getId(), photoObjectKeys, videoObjectKeys, audioObjectKeys);
    }

    private boolean sameItems(List<String> current, List<String> requested) {
        return requested != null && current.size() == requested.size()
                && new HashSet<>(current).equals(new HashSet<>(requested));
    }

    /** 老板浏览目录：分页列出已发布名片的卡片，支持按游戏标签/关键字过滤，见 {@link CompanionShowcasePublicCardView}。 */
    public PageResult<CompanionShowcasePublicCardView> browsePublished(int pageNum, int pageSize, Long tagId,
                                                                        String keyword) {
        PageResult<CompanionShowcasePublicSummary> page = showcaseRepository.searchPublished(pageNum, pageSize,
                tagId, keyword);
        List<CompanionShowcasePublicCardView> cards = page.records().stream().map(this::toCardView).toList();
        return PageResult.of(cards, page.total(), page.pageNum(), page.pageSize());
    }

    private CompanionShowcasePublicCardView toCardView(CompanionShowcasePublicSummary summary) {
        List<TagView> tags = tagDefinitionRepository.findByIds(summary.tagIds()).stream()
                .map(TagApplicationService::toView)
                .toList();
        String avatarUrl = summary.avatarObjectKey() == null ? null
                : objectStorageService.presignedDownloadUrl(summary.avatarObjectKey(), MEDIA_URL_TTL);
        String coverPhotoUrl = summary.coverPhotoObjectKey() == null ? null
                : objectStorageService.presignedDownloadUrl(summary.coverPhotoObjectKey(), MEDIA_URL_TTL);
        return new CompanionShowcasePublicCardView(summary.userId(), summary.nickname(), avatarUrl, summary.bio(),
                summary.tagline(), summary.availability(), tags, coverPhotoUrl, summary.publishedAt());
    }

    private User requireCompanion(Long userId) {
        User user = getUser(userId);
        if (!user.hasRole(Role.COMPANION)) {
            throw IdentityException.companionShowcaseRoleRequired(userId);
        }
        return user;
    }

    /** 每个标签都要求存在且当前启用，否则拒绝整个请求，跟 {@link CompanionApplicationService#validateTags} 同一套校验。 */
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

    private void validateMediaLimit(String label, List<String> objectKeys, int maxCount) {
        if (objectKeys != null && objectKeys.size() > maxCount) {
            throw IdentityException.companionShowcaseMediaLimitExceeded(label, maxCount);
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> IdentityException.notFound(userId));
    }

    private List<String> toUrls(List<String> objectKeys) {
        return objectKeys.stream().map(key -> objectStorageService.presignedDownloadUrl(key, MEDIA_URL_TTL)).toList();
    }

    private CompanionShowcaseView toView(CompanionShowcaseRevision revision) {
        User applicant = getUser(revision.getUserId());
        List<TagView> tags = tagDefinitionRepository.findByIds(revision.getTagIds()).stream()
                .map(TagApplicationService::toView)
                .toList();
        String reviewerNickname = revision.getReviewerId() == null ? null
                : userRepository.findById(revision.getReviewerId()).map(User::getNickname).orElse(null);
        CompanionShowcase showcase = showcaseRepository.findByUserId(revision.getUserId()).orElse(null);
        boolean published = showcase != null && showcase.isPublished();
        OffsetDateTime publishedAt = showcase == null ? null : showcase.getPublishedAt();
        return new CompanionShowcaseView(revision.getId(), revision.getUserId(), applicant.getNickname(),
                applicant.getPhone(), applicant.getEmail(), revision.getStatus(), revision.getBio(),
                revision.getTagline(), revision.getAvailability(),
                revision.getCoverObjectKey() == null ? null : objectStorageService.presignedDownloadUrl(revision.getCoverObjectKey(), MEDIA_URL_TTL),
                revision.getCoverObjectKey(), tags,
                toUrls(revision.getPhotoObjectKeys()), toUrls(revision.getVideoObjectKeys()),
                toUrls(revision.getAudioObjectKeys()), revision.getPhotoObjectKeys(), revision.getVideoObjectKeys(),
                revision.getAudioObjectKeys(), revision.getReviewerId(), reviewerNickname,
                revision.getReviewComment(), revision.getReviewedAt(), revision.getCreatedAt(),
                revision.getUpdatedAt(), published, publishedAt);
    }
}
