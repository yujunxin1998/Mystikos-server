package com.mystikos.common.region;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 行政区划查询，只读。供前端渲染树状选择器，也供其他模块（目前是
 * mystikos-identity 的 User.regionCode）校验一个 code 是否存在。
 */
@Service
public class RegionQueryService {

    private final AdministrativeRegionMapper regionMapper;

    public RegionQueryService(AdministrativeRegionMapper regionMapper) {
        this.regionMapper = regionMapper;
    }

    /** 全量两层树：国家节点下挂它的一级行政区。 */
    public List<RegionNodeView> getTree() {
        List<AdministrativeRegionPO> all = regionMapper.selectList(
                new LambdaQueryWrapper<AdministrativeRegionPO>().orderByAsc(AdministrativeRegionPO::getSortOrder));

        Map<String, List<AdministrativeRegionPO>> childrenByParent = all.stream()
                .filter(po -> po.getParentCode() != null)
                .collect(Collectors.groupingBy(AdministrativeRegionPO::getParentCode));

        return all.stream()
                .filter(po -> po.getParentCode() == null)
                .map(country -> toView(country, childrenByParent.getOrDefault(country.getCode(), List.of())))
                .toList();
    }

    /** 校验一个行政区划编码是否存在（国家或一级行政区均可），供其他模块给用户资料做外键校验。 */
    public boolean exists(String code) {
        return code != null && regionMapper.selectById(code) != null;
    }

    private static RegionNodeView toView(AdministrativeRegionPO po, List<AdministrativeRegionPO> children) {
        List<RegionNodeView> childViews = children.stream()
                .map(child -> toView(child, List.of()))
                .toList();
        return new RegionNodeView(po.getCode(), po.getParentCode(), po.getLevel(), po.getNameZh(), po.getNameEn(),
                po.getSortOrder(), childViews);
    }
}
