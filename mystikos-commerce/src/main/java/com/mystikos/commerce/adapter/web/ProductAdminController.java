package com.mystikos.commerce.adapter.web;

import com.mystikos.commerce.adapter.web.dto.CreateProductRequest;
import com.mystikos.commerce.application.command.CreateProductCommand;
import com.mystikos.commerce.application.service.CommerceApplicationService;
import com.mystikos.common.result.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台管理接口：新增商品。归入 {@code /api/v1/manage/**} 路由前缀，商品只能在后台创建，
 * C 端浏览接口见 {@link ProductController}。
 */
@RestController
@RequestMapping("/api/v1/manage/products")
@Tag(name = "后台管理 - 商品管理", description = "商品新增，运营态操作")
public class ProductAdminController {

    private final CommerceApplicationService commerceApplicationService;

    public ProductAdminController(CommerceApplicationService commerceApplicationService) {
        this.commerceApplicationService = commerceApplicationService;
    }

    @PostMapping
    @Operation(summary = "新增商品", description = "图片需先调用 POST /api/v1/files/upload 逐张上传，"
            + "拿到的 objectKey/URL 填进 images；创建后默认上架，并按 initialStock 初始化库存")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Long> create(@Valid @RequestBody CreateProductRequest request) {
        Long productId = commerceApplicationService.createProduct(new CreateProductCommand(
                request.getCategoryId(),
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getImages(),
                request.getInitialStock()));
        return APIResponse.ok(productId);
    }
}
