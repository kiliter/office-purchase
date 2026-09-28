package com.office.purchase.controller;

import com.office.purchase.annotation.RequireRole;
import com.office.purchase.common.Result;
import com.office.purchase.common.RoleConst;
import com.office.purchase.common.UserContext;
import com.office.purchase.dto.OrderStatusDTO;
import com.office.purchase.service.PurchaseOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 采购订单接口。
 */
@RestController
@RequestMapping("/order")
public class PurchaseOrderController {

    @Resource
    private PurchaseOrderService purchaseOrderService;

    /**
     * 分页查询订单。员工只返回自己的订单。
     */
    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1") long current,
                          @RequestParam(defaultValue = "10") long size,
                          @RequestParam(required = false) String orderStatus,
                          @RequestParam(required = false) String keyword) {
        return Result.ok(purchaseOrderService.pageOrders(UserContext.get(), current, size, orderStatus, keyword));
    }

    /**
     * 管理员更新订单履约状态。
     */
    @PutMapping("/status")
    @RequireRole(RoleConst.ADMIN)
    public Result<Void> status(@Valid @RequestBody OrderStatusDTO dto) {
        purchaseOrderService.updateStatus(dto.getOrderId(), dto.getOrderStatus());
        return Result.ok("订单状态已更新", null);
    }
}
