package com.mystikos.gifting.adapter.web;

import com.mystikos.common.result.APIResponse;
import com.mystikos.common.security.CurrentUserContext;
import com.mystikos.gifting.adapter.web.dto.GiftCatalogItemView;
import com.mystikos.gifting.adapter.web.dto.SendGiftRequest;
import com.mystikos.gifting.application.command.SendGiftCommand;
import com.mystikos.gifting.application.service.GiftApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gifts")
@Tag(name = "礼物打赏", description = "礼物目录、赠礼")
public class GiftController {

    private final GiftApplicationService giftApplicationService;

    public GiftController(GiftApplicationService giftApplicationService) {
        this.giftApplicationService = giftApplicationService;
    }

    @GetMapping("/catalog")
    @Operation(summary = "查询礼物目录", description = "只返回上架中的礼物")
    public APIResponse<List<GiftCatalogItemView>> catalog() {
        return APIResponse.ok(giftApplicationService.listCatalog().stream()
                .map(GiftCatalogItemView::from)
                .toList());
    }

    @PostMapping("/send")
    @Operation(summary = "赠礼", description = "赠送方是当前登录用户；解锁条件不满足或规则暂不支持评估会拒绝")
    @SecurityRequirement(name = "bearerAuth")
    public APIResponse<Long> send(@Valid @RequestBody SendGiftRequest request) {
        Long patronId = Long.valueOf(CurrentUserContext.get().userId());
        Long transactionId = giftApplicationService.sendGift(new SendGiftCommand(
                patronId, request.getCompanionId(), request.getGiftId(), request.getQuantity()));
        return APIResponse.ok(transactionId);
    }
}
