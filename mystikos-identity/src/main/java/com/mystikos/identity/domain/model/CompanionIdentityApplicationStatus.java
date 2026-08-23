package com.mystikos.identity.domain.model;

/**
 * 陪玩身份申请状态：申请中 -&gt; 考核中 -&gt; 审核通过/审核未通过。考核过程本身不在系统里体现
 * （线下人工评估），系统只记录状态流转和最终的 {@link AssessmentResult}。
 */
public enum CompanionIdentityApplicationStatus {
    SUBMITTED,
    IN_ASSESSMENT,
    APPROVED,
    REJECTED
}
