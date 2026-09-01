package com.mystikos.gifting.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.gifting.adapter.web.dto.GiftCatalogItemView;
import com.mystikos.gifting.adapter.web.dto.GiftTierView;
import com.mystikos.gifting.adapter.web.dto.SaveGiftRequest;
import com.mystikos.gifting.adapter.web.dto.SaveGiftTierRequest;
import com.mystikos.gifting.application.command.SaveGiftCommand;
import com.mystikos.gifting.application.command.SaveGiftTierCommand;
import com.mystikos.gifting.application.service.GiftApplicationService;
import com.mystikos.gifting.domain.model.GiftTier;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 后台管理接口：礼物目录、档位的新增/编辑，以及赠礼流水退款。全部是运营配置数据的
 * 直接读写——新增一件礼物、调整某档位倍率都只是这几个接口的一次调用，不需要发版。
 */
@RestController
@RequestMapping("/api/v1/manage")
@Tag(name = "后台管理 - 礼物", description = "礼物目录/档位维护、赠礼退款")
public class GiftAdminController {

    private final GiftApplicationService giftApplicationService;

    public GiftAdminController(GiftApplicationService giftApplicationService) {
        this.giftApplicationService = giftApplicationService;
    }

    @GetMapping("/gifts")
    @Operation(summary = "查询礼物目录（后台）", description = "含已下架的礼物")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<List<GiftCatalogItemView>> listGifts() {
        Map<Long, GiftTier> tiersById = giftApplicationService.listAllTiersForAdmin().stream()
                .collect(Collectors.toMap(GiftTier::getId, Function.identity()));
        return APIResponse.ok(giftApplicationService.listAllGiftsForAdmin().stream()
                .map(item -> GiftCatalogItemView.from(item, tiersById.get(item.getTierId())))
                .toList());
    }

    @PostMapping("/gifts")
    @Operation(summary = "新增礼物")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Long> createGift(@Valid @RequestBody SaveGiftRequest request) {
        return APIResponse.ok(giftApplicationService.saveGift(toCommand(null, request)));
    }

    @PutMapping("/gifts/{giftId}")
    @Operation(summary = "编辑礼物", description = "整行覆盖式更新")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Long> updateGift(@Parameter(description = "礼物ID") @PathVariable Long giftId,
                                         @Valid @RequestBody SaveGiftRequest request) {
        return APIResponse.ok(giftApplicationService.saveGift(toCommand(giftId, request)));
    }

    @GetMapping("/gift-tiers")
    @Operation(summary = "查询礼物档位（后台）", description = "含已停用的档位")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<List<GiftTierView>> listTiers() {
        return APIResponse.ok(giftApplicationService.listAllTiersForAdmin().stream()
                .map(GiftTierView::from)
                .toList());
    }

    @PostMapping("/gift-tiers")
    @Operation(summary = "新增礼物档位")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Long> createTier(@Valid @RequestBody SaveGiftTierRequest request) {
        return APIResponse.ok(giftApplicationService.saveGiftTier(toTierCommand(null, request)));
    }

    @PutMapping("/gift-tiers/{tierId}")
    @Operation(summary = "编辑礼物档位", description = "调整倍率只影响此后新发生的赠礼，历史流水已快照倍率不受影响")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Long> updateTier(@Parameter(description = "档位ID") @PathVariable Long tierId,
                                         @Valid @RequestBody SaveGiftTierRequest request) {
        return APIResponse.ok(giftApplicationService.saveGiftTier(toTierCommand(tierId, request)));
    }

    @PostMapping("/gift-transactions/{transactionId}/refund")
    @Operation(summary = "赠礼退款", description = "反向钱包转账并同步扣减亲密度/VIP 累计值，只能退一次")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Void> refund(@Parameter(description = "赠礼流水ID") @PathVariable Long transactionId) {
        giftApplicationService.refundGiftTransaction(transactionId);
        return APIResponse.ok(null);
    }

    private SaveGiftCommand toCommand(Long id, SaveGiftRequest request) {
        return new SaveGiftCommand(id, request.getCode(), request.getName(), request.getIcon(),
                request.getPrice(), request.getTierId(), request.getUnlockRuleType(),
                request.getUnlockRuleThreshold(), request.getActive() == null || request.getActive());
    }

    private SaveGiftTierCommand toTierCommand(Long id, SaveGiftTierRequest request) {
        return new SaveGiftTierCommand(id, request.getCode(), request.getDisplayName(), request.getDisplayNameEn(),
                request.getMultiplier(), request.getSortOrder(), request.getActive() == null || request.getActive());
    }
}
