package com.mystikos.common.dict;

/** 字典单项：一个枚举常量对应的 code + 展示名。 */
public record DictItem(String code, String displayName) {

    public static DictItem of(DictEnum value) {
        return new DictItem(value.getCode(), value.getDisplayName());
    }
}
