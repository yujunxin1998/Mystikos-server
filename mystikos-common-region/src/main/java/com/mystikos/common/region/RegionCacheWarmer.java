package com.mystikos.common.region;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** 应用启动完成后主动查一次库、把行政区划树写入 Redis，避免第一个请求打库现查。 */
@Component
public class RegionCacheWarmer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RegionCacheWarmer.class);

    private final RegionQueryService regionQueryService;

    public RegionCacheWarmer(RegionQueryService regionQueryService) {
        this.regionQueryService = regionQueryService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            int count = regionQueryService.reloadTreeCache().size();
            log.info("行政区划树缓存预热完成，国家节点数：{}", count);
        } catch (Exception e) {
            log.warn("行政区划树缓存预热失败，不影响启动，首次请求会现查库回填", e);
        }
    }
}
