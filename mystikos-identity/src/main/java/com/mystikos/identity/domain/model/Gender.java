package com.mystikos.identity.domain.model;

import com.mystikos.common.dict.DictEnum;

/** 用户自报性别，不用于任何法律/年龄判定，仅作资料展示。默认不愿透露。 */
public enum Gender implements DictEnum {
    MALE("MALE", "男"),
    FEMALE("FEMALE", "女"),
    UNDISCLOSED("UNDISCLOSED", "不透露");

    private final String code;
    private final String displayName;

    Gender(String code, String displayName) {
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
