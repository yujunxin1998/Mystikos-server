package com.mystikos.common.region;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 行政区划查询，只读。供前端渲染树状选择器，也供其他模块（目前是
 * mystikos-identity 的 User.regionCode）校验一个 code 是否存在。
 *
 * <p>{@code getTree()} 走 Redis 缓存（cache-aside，不设过期时间——这是变动很少的参考数据，
 * 靠显式失效而不是 TTL 保鲜）：应用启动时由 {@link RegionCacheWarmer} 主动查库写入缓存一次；
 * 之后谁改了 {@code common_region} 表数据，负责调用 {@link #evictTreeCache()} 让下一次
 * {@link #getTree()} 重新查库回填——目前代码里还没有会改这张表的写接口（纯 Flyway 种子数据），
 * 这个方法先留给以后接管理台改行政区划时用。
 */
@Service
public class RegionQueryService {

    static final String TREE_CACHE_KEY = "common:region:tree";

    private final AdministrativeRegionMapper regionMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public RegionQueryService(AdministrativeRegionMapper regionMapper, RedisTemplate<String, Object> redisTemplate) {
        this.regionMapper = regionMapper;
        this.redisTemplate = redisTemplate;
    }

    /** 全量两层树：国家节点下挂它的一级行政区。优先读缓存，未命中才查库回填。 */
    @SuppressWarnings("unchecked")
    public List<RegionNodeView> getTree() {
        Object cached = redisTemplate.opsForValue().get(TREE_CACHE_KEY);
        if (cached != null) {
            return (List<RegionNodeView>) cached;
        }
        return reloadTreeCache();
    }

    /** 查库重建整棵树并覆盖写入缓存，返回最新结果——应用启动预热、以及数据变更后失效重建都调这个。 */
    public List<RegionNodeView> reloadTreeCache() {
        List<AdministrativeRegionPO> all = regionMapper.selectList(
                new LambdaQueryWrapper<AdministrativeRegionPO>().orderByAsc(AdministrativeRegionPO::getSortOrder));

        Map<String, List<AdministrativeRegionPO>> childrenByParent = all.stream()
                .filter(po -> po.getParentCode() != null)
                .collect(Collectors.groupingBy(AdministrativeRegionPO::getParentCode));

        List<RegionNodeView> tree = all.stream()
                .filter(po -> po.getParentCode() == null)
                .map(country -> toView(country, childrenByParent.getOrDefault(country.getCode(), List.of())))
                .collect(Collectors.toCollection(ArrayList::new));

        redisTemplate.opsForValue().set(TREE_CACHE_KEY, tree);
        return tree;
    }

    /** 行政区划数据被改了之后调用，删掉缓存，下一次 getTree() 会重新查库回填。 */
    public void evictTreeCache() {
        redisTemplate.delete(TREE_CACHE_KEY);
    }

    /** 校验一个行政区划编码是否存在（国家或一级行政区均可），供其他模块给用户资料做外键校验。 */
    public boolean exists(String code) {
        return code != null && regionMapper.selectById(code) != null;
    }

    /**
     * 这里的 {@code List<RegionNodeView>} 必须是真正的 {@link ArrayList}，不能用
     * {@code Stream.toList()}/{@code List.of()}——那两个返回的是 JDK 内部不可变 List 实现
     * （{@code java.util.ImmutableCollections$ListN}），Redis 缓存那边的 Jackson 多态序列化
     * 能把类名写出去，但反序列化时找不到这个类的可用构造器会直接抛异常
     * （MismatchedInputException: Unexpected token START_ARRAY）。ArrayList 是 Jackson 认识的
     * List 默认实现，不会有这个问题。
     */
    private static RegionNodeView toView(AdministrativeRegionPO po, List<AdministrativeRegionPO> children) {
        List<RegionNodeView> childViews = children.stream()
                .map(child -> toView(child, List.of()))
                .collect(Collectors.toCollection(ArrayList::new));
        return new RegionNodeView(po.getCode(), po.getParentCode(), po.getLevel(), po.getNameZh(), po.getNameEn(),
                po.getSortOrder(), childViews);
    }
}
