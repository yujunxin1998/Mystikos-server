package com.mystikos.identity.domain.model;

import com.mystikos.common.dict.DictEnum;

/**
 * 陪玩身份申请状态：申请中 -&gt; 考核中 -&gt; 审核通过/审核未通过。考核过程本身不在系统里体现
 * （线下人工评估），系统只记录状态流转和最终的 {@link AssessmentResult}。
 */
public enum CompanionIdentityApplicationStatus implements DictEnum {
    SUBMITTED("SUBMITTED", "申请中"),
    IN_ASSESSMENT("IN_ASSESSMENT", "考核中"),
    APPROVED("APPROVED", "审核通过"),
    REJECTED("REJECTED", "审核未通过");

    private final String code;
    private final String displayName;

    CompanionIdentityApplicationStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}
