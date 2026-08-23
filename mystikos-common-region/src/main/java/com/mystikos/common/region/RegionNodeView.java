package com.mystikos.common.region;

import java.util.List;

/** 树形节点视图，country 节点的 children 是它下属的一级行政区，subdivision 节点 children 恒为空。 */
public record RegionNodeView(
        String code,
        String parentCode,
        String level,
        String nameZh,
        String nameEn,
        int sortOrder,
        List<RegionNodeView> children
) {
}
