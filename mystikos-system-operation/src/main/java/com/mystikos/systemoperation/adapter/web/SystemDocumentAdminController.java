package com.mystikos.systemoperation.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.common.result.PageResult;
import com.mystikos.common.security.CurrentUserContext;
import com.mystikos.systemoperation.adapter.web.dto.UpdateSystemDocumentRequest;
import com.mystikos.systemoperation.application.service.SystemDocumentApplicationService;
import com.mystikos.systemoperation.application.service.SystemDocumentRevisionView;
import com.mystikos.systemoperation.application.service.SystemDocumentView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台管理接口：系统文档（法律条款等）的新建/更新/历史查询。
 * 归入 {@code /api/v1/manage/**} 路由前缀，见 {@code SecurityConfig} 类注释。
 */
@RestController
@RequestMapping("/api/v1/manage/system-documents")
@Tag(name = "后台管理 - 系统文档", description = "法律条款等系统内容文档的维护")
public class SystemDocumentAdminController {

    private final SystemDocumentApplicationService systemDocumentApplicationService;

    public SystemDocumentAdminController(SystemDocumentApplicationService systemDocumentApplicationService) {
        this.systemDocumentApplicationService = systemDocumentApplicationService;
    }

    @GetMapping
    @Operation(summary = "查询系统文档列表")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<List<SystemDocumentView>> listAll() {
        return APIResponse.ok(systemDocumentApplicationService.listAll());
    }

    @GetMapping("/{code}")
    @Operation(summary = "查询系统文档详情")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<SystemDocumentView> get(@Parameter(description = "文档编码") @PathVariable String code) {
        return APIResponse.ok(systemDocumentApplicationService.get(code));
    }

    @PutMapping("/{code}")
    @Operation(summary = "新建或更新系统文档", description = "文档不存在则新建（版本从1开始），存在则更新内容并把版本号加一")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<SystemDocumentView> createOrUpdate(
            @Parameter(description = "文档编码，自由字符串，如 TERMS_OF_SERVICE") @PathVariable String code,
            @Valid @RequestBody UpdateSystemDocumentRequest request) {
        String operatorId = CurrentUserContext.get().userId();
        return APIResponse.ok(systemDocumentApplicationService.createOrUpdate(
                code, request.getTitle(), request.getContent(), operatorId));
    }

    @GetMapping("/{code}/revisions")
    @Operation(summary = "查询系统文档修订历史", description = "按版本号倒序分页")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<PageResult<SystemDocumentRevisionView>> listRevisions(
            @Parameter(description = "文档编码") @PathVariable String code,
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        return APIResponse.ok(systemDocumentApplicationService.listRevisions(code, pageNum, pageSize));
    }
}
