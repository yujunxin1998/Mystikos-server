package com.mystikos.systemoperation.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.systemoperation.application.service.SystemDocumentApplicationService;
import com.mystikos.systemoperation.application.service.SystemDocumentView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统文档只读公开查询，给前端展示用户协议/隐私政策这类页面用，不鉴权。
 * 后台编辑入口见 {@link SystemDocumentAdminController}。
 */
@RestController
@RequestMapping("/api/v1/system-documents")
@Tag(name = "系统运营 - 系统文档", description = "法律条款等系统内容文档，公开只读查询")
public class SystemDocumentController {

    private final SystemDocumentApplicationService systemDocumentApplicationService;

    public SystemDocumentController(SystemDocumentApplicationService systemDocumentApplicationService) {
        this.systemDocumentApplicationService = systemDocumentApplicationService;
    }

    @GetMapping("/{code}")
    @Operation(summary = "查询系统文档当前内容", description = "文档编码如 TERMS_OF_SERVICE/PRIVACY_POLICY")
    public APIResponse<SystemDocumentView> get(@Parameter(description = "文档编码") @PathVariable String code) {
        return APIResponse.ok(systemDocumentApplicationService.get(code));
    }
}
