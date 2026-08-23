package com.mystikos.commerce.adapter.web;

import com.mystikos.commerce.adapter.web.dto.AddToCartRequest;
import com.mystikos.commerce.adapter.web.dto.CartLineResponse;
import com.mystikos.commerce.application.command.AddToCartCommand;
import com.mystikos.commerce.application.service.CommerceApplicationService;
import com.mystikos.common.result.APIResponse;
import com.mystikos.common.security.CurrentUserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "商城", description = "购物车")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CommerceApplicationService commerceApplicationService;

    public CartController(CommerceApplicationService commerceApplicationService) {
        this.commerceApplicationService = commerceApplicationService;
    }

    @GetMapping
    @Operation(summary = "查看购物车")
    public APIResponse<List<CartLineResponse>> get() {
        return APIResponse.ok(commerceApplicationService.getCart(currentPatronId()).stream()
                .map(CartLineResponse::from)
                .toList());
    }

    @PostMapping
    @Operation(summary = "加入购物车", description = "商品已在车里时数量累加")
    public APIResponse<Void> add(@Valid @RequestBody AddToCartRequest request) {
        commerceApplicationService.addToCart(new AddToCartCommand(
                currentPatronId(), request.getProductId(), request.getQuantity()));
        return APIResponse.ok();
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "移出购物车")
    public APIResponse<Void> remove(@Parameter(description = "商品ID") @PathVariable Long productId) {
        commerceApplicationService.removeFromCart(currentPatronId(), productId);
        return APIResponse.ok();
    }

    private Long currentPatronId() {
        return Long.valueOf(CurrentUserContext.get().userId());
    }
}
