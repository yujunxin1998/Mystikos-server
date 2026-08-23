package com.mystikos.identity.domain.model;

/**
 * 陪玩名片草稿/提交记录状态：编辑中 -&gt; 待审核 -&gt; 审核通过/审核未通过。
 * 通过后台账（{@link CompanionShowcase}）才会指向这条 revision 对外展示，见该类注释。
 */
public enum CompanionShowcaseRevisionStatus {
    DRAFT,
    PENDING_REVIEW,
    APPROVED,
    REJECTED
}
