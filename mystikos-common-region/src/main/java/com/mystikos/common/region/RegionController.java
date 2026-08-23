package com.mystikos.common.region;

import com.mystikos.common.result.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 行政区划参考数据，只读、公开，不属于任何业务限界上下文（同 mystikos-common-storage 的定位）。
 */
@RestController
@RequestMapping("/api/v1/regions")
@Tag(name = "行政区划", description = "国家 + 一级行政区参考数据，供前端渲染树状选择器")
public class RegionController {

    private final RegionQueryService regionQueryService;

    public RegionController(RegionQueryService regionQueryService) {
        this.regionQueryService = regionQueryService;
    }

    @GetMapping("/tree")
    @Operation(summary = "查询行政区划树", description = "两层：国家 -> 一级行政区（邦/州/大区）")
    public APIResponse<List<RegionNodeView>> tree() {
        return APIResponse.ok(regionQueryService.getTree());
    }
}
