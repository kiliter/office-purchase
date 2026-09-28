package com.office.purchase.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理员更新订单履约状态。
 */
@Data
public class OrderStatusDTO {

    @NotNull(message = "缺少订单编号")
    private Long orderId;

    @NotBlank(message = "请选择订单状态")
    private String orderStatus;
}
