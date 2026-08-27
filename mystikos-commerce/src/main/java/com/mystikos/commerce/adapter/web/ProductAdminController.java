package com.mystikos.commerce.adapter.web;

import com.mystikos.commerce.adapter.web.dto.CreateProductRequest;
import com.mystikos.commerce.adapter.web.dto.ProductView;
import com.mystikos.commerce.adapter.web.dto.UpdateProductRequest;
import com.mystikos.commerce.application.command.CreateProductCommand;
import com.mystikos.commerce.application.command.UpdateProductCommand;
import com.mystikos.commerce.application.service.CommerceApplicationService;
import com.mystikos.commerce.domain.model.Product;
import com.mystikos.commerce.domain.model.ProductStatus;
import com.mystikos.common.result.APIResponse;
import com.mystikos.common.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台管理接口：商品的新增/查询/编辑。归入 {@code /api/v1/manage/**} 路由前缀，商品只能在后台创建和维护，
 * C 端浏览接口见 {@link ProductController}（只返回上架中的商品）。
 */
@RestController
@RequestMapping("/api/v1/manage/products")
@Tag(name = "后台管理 - 商品管理", description = "商品新增/查询/编辑，运营态操作")
public class ProductAdminController {

    private final CommerceApplicationService commerceApplicationService;

    public ProductAdminController(CommerceApplicationService commerceApplicationService) {
        this.commerceApplicationService = commerceApplicationService;
    }

    @GetMapping
    @Operation(summary = "分页查询商品", description = "不限上下架状态，可按 status 过滤；不传 status 返回全部")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<PageResult<ProductView>> list(
            @Parameter(description = "上下架状态，不传则不限") @RequestParam(required = false) ProductStatus status,
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<Product> page = commerceApplicationService.listProductsForAdmin(status, pageNum, pageSize);
        List<ProductView> views = page.records().stream().map(ProductView::from).toList();
        return APIResponse.ok(PageResult.of(views, page.total(), page.pageNum(), page.pageSize()));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "商品详情（后台）", description = "不限上下架状态，用于编辑前加载数据")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<ProductView> get(@Parameter(description = "商品ID") @PathVariable Long productId) {
        return APIResponse.ok(ProductView.from(commerceApplicationService.getProduct(productId)));
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

    @PutMapping("/{productId}")
    @Operation(summary = "编辑商品", description = "整体覆盖式更新名称/描述/价格/图片/分类；"
            + "status 不传则保持原状态，传了则同时完成上下架切换；不涉及库存调整")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<ProductView> update(@Parameter(description = "商品ID") @PathVariable Long productId,
                                            @Valid @RequestBody UpdateProductRequest request) {
        Product updated = commerceApplicationService.updateProduct(productId, new UpdateProductCommand(
                request.getCategoryId(),
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getImages(),
                request.getStatus()));
        return APIResponse.ok(ProductView.from(updated));
    }
}
