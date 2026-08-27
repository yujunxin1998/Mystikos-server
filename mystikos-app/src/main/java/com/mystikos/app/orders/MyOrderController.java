package com.mystikos.app.orders;

import com.mystikos.common.result.APIResponse;
import com.mystikos.common.result.PageResult;
import com.mystikos.common.security.CurrentUserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/my-orders")
@Tag(name = "我的订单", description = "个人订单预览：聚合陪玩订单（Booking）与商品订单（Commerce），只能查看当前登录账号自己的订单")
@SecurityRequirement(name = "bearerAuth")
public class MyOrderController {

    private final MyOrderApplicationService myOrderApplicationService;

    public MyOrderController(MyOrderApplicationService myOrderApplicationService) {
        this.myOrderApplicationService = myOrderApplicationService;
    }

    @GetMapping
    @Operation(summary = "我的订单预览", description = "合并陪玩订单与商品订单，按下单时间倒序分页；只能查看当前登录账号自己的订单")
    public APIResponse<PageResult<MyOrderView>> listMine(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        Long patronId = Long.valueOf(CurrentUserContext.get().userId());
        return APIResponse.ok(myOrderApplicationService.listMyOrders(patronId, pageNum, pageSize));
    }
}
