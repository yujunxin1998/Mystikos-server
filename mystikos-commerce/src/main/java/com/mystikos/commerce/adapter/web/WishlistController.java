package com.mystikos.commerce.adapter.web;

import com.mystikos.commerce.adapter.web.dto.AddToWishlistRequest;
import com.mystikos.commerce.adapter.web.dto.WishlistLineResponse;
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
@RequestMapping("/api/v1/wishlist")
@Tag(name = "商城", description = "心愿单")
@SecurityRequirement(name = "bearerAuth")
public class WishlistController {

    private final CommerceApplicationService commerceApplicationService;

    public WishlistController(CommerceApplicationService commerceApplicationService) {
        this.commerceApplicationService = commerceApplicationService;
    }

    @GetMapping
    @Operation(summary = "查看心愿单")
    public APIResponse<List<WishlistLineResponse>> get() {
        return APIResponse.ok(commerceApplicationService.getWishlist(currentPatronId()).stream()
                .map(WishlistLineResponse::from)
                .toList());
    }

    @PostMapping
    @Operation(summary = "加入心愿单", description = "重复加入同一商品不报错，幂等")
    public APIResponse<Void> add(@Valid @RequestBody AddToWishlistRequest request) {
        commerceApplicationService.addToWishlist(currentPatronId(), request.getProductId());
        return APIResponse.ok();
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "移出心愿单")
    public APIResponse<Void> remove(@Parameter(description = "商品ID") @PathVariable Long productId) {
        commerceApplicationService.removeFromWishlist(currentPatronId(), productId);
        return APIResponse.ok();
    }

    private Long currentPatronId() {
        return Long.valueOf(CurrentUserContext.get().userId());
    }
}
