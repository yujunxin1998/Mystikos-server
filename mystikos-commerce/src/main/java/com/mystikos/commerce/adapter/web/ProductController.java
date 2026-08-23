package com.mystikos.commerce.adapter.web;

import com.mystikos.commerce.adapter.web.dto.ProductView;
import com.mystikos.commerce.application.service.CommerceApplicationService;
import com.mystikos.common.result.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "商城", description = "商品目录")
public class ProductController {

    private final CommerceApplicationService commerceApplicationService;

    public ProductController(CommerceApplicationService commerceApplicationService) {
        this.commerceApplicationService = commerceApplicationService;
    }

    @GetMapping
    @Operation(summary = "商品列表", description = "只返回上架中的商品")
    public APIResponse<List<ProductView>> list() {
        return APIResponse.ok(commerceApplicationService.listProducts().stream()
                .map(ProductView::from)
                .toList());
    }

    @GetMapping("/{productId}")
    @Operation(summary = "商品详情")
    public APIResponse<ProductView> get(@Parameter(description = "商品ID") @PathVariable Long productId) {
        return APIResponse.ok(ProductView.from(commerceApplicationService.getProduct(productId)));
    }
}
