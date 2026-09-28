package com.office.purchase.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 新增或修改办公用品。修改时必须带 goodsId。
 */
@Data
public class GoodsSaveDTO {

    private Long goodsId;

    @NotBlank(message = "请输入商品名称")
    @Size(max = 100, message = "商品名称不能超过 100 个字")
    private String goodsName;

    @NotBlank(message = "请输入商品类别")
    @Size(max = 50, message = "商品类别不能超过 50 个字")
    private String goodsType;

    @Size(max = 100, message = "规格不能超过 100 个字")
    private String spec;

    @NotNull(message = "请输入单价")
    @DecimalMin(value = "0.00", message = "单价不能为负数")
    @Digits(integer = 8, fraction = 2, message = "单价格式不正确")
    private BigDecimal price;

    @NotNull(message = "请输入库存")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;
}
