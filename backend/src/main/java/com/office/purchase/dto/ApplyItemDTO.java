package com.office.purchase.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 申请明细中的一种商品。
 */
@Data
public class ApplyItemDTO {

    @NotNull(message = "请选择商品")
    private Long goodsId;

    @NotNull(message = "请填写采购数量")
    @Min(value = 1, message = "采购数量至少为 1")
    @Max(value = 100000, message = "采购数量过大")
    private Integer buyNum;
}
