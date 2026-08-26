package com.mystikos.systemoperation.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.common.result.PageResult;
import com.mystikos.systemoperation.application.service.OperationLogApplicationService;
import com.mystikos.systemoperation.application.service.OperationLogView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

/**
 * 后台操作日志查询。日志本身由 {@code OperationLogInterceptor} 对 {@code /api/v1/manage/**}
 * 下的非 GET 请求自动记录，这里只提供检索，不提供手工新增/编辑（操作日志不允许篡改）。
 */
@RestController
@RequestMapping("/api/v1/manage/operation-logs")
@Tag(name = "后台管理 - 操作日志", description = "后台管理操作留痕检索")
public class OperationLogAdminController {

    private final OperationLogApplicationService operationLogApplicationService;

    public OperationLogAdminController(OperationLogApplicationService operationLogApplicationService) {
        this.operationLogApplicationService = operationLogApplicationService;
    }

    @GetMapping
    @Operation(summary = "分页查询操作日志", description = "按发生时间倒序，支持按操作人/路径关键字/时间范围过滤")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<PageResult<OperationLogView>> list(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "操作人用户ID") @RequestParam(required = false) String operatorId,
            @Parameter(description = "请求路径关键字") @RequestParam(required = false) String pathKeyword,
            @Parameter(description = "发生时间范围-起（ISO-8601）")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime occurredFrom,
            @Parameter(description = "发生时间范围-止（ISO-8601）")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime occurredTo) {
        return APIResponse.ok(operationLogApplicationService.listPage(
                operatorId, pathKeyword, occurredFrom, occurredTo, pageNum, pageSize));
    }
}
