package com.mystikos.common.dict;

import java.util.Arrays;
import java.util.List;

/** 一个字典分类（对应一个枚举类型）及其全部条目，按枚举声明顺序排列。 */
public record DictCatalog(String code, String name, List<DictItem> items) {

    /** {@code values} 需要按业务展示顺序传入（一般就是 {@code EnumClass.values()}）。 */
    public static DictCatalog of(String code, String name, DictEnum[] values) {
        return new DictCatalog(code, name, Arrays.stream(values).map(DictItem::of).toList());
    }
}
