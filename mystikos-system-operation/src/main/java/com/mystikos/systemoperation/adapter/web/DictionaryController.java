package com.mystikos.systemoperation.adapter.web;

import com.mystikos.common.dict.DictCatalog;
import com.mystikos.common.result.APIResponse;
import com.mystikos.systemoperation.application.service.DictionaryApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典查询，只读、公开接口。数据来自各上下文的 {@code DictSource} Bean 聚合，
 * 不查表——枚举本身是权威来源，见 {@link DictionaryApplicationService}。
 */
@RestController
@RequestMapping("/api/v1/dicts")
@Tag(name = "系统运营 - 字典", description = "枚举字典聚合查询")
public class DictionaryController {

    private final DictionaryApplicationService dictionaryApplicationService;

    public DictionaryController(DictionaryApplicationService dictionaryApplicationService) {
        this.dictionaryApplicationService = dictionaryApplicationService;
    }

    @GetMapping
    @Operation(summary = "查询全部字典", description = "按各业务模块贡献的枚举聚合，一次性返回所有分类")
    public APIResponse<List<DictCatalog>> listAll() {
        return APIResponse.ok(dictionaryApplicationService.listAll());
    }

    @GetMapping("/{code}")
    @Operation(summary = "查询单个字典分类", description = "分类编码如 ROLE/GENDER/BOOKING_STATUS")
    public APIResponse<DictCatalog> get(@Parameter(description = "字典分类编码") @PathVariable String code) {
        return APIResponse.ok(dictionaryApplicationService.get(code));
    }
}
