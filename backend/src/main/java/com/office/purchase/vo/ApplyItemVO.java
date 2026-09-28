package com.office.purchase.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 申请明细展示对象。金额按商品当前单价乘以申请数量计算。
 */
@Data
public class ApplyItemVO {

    private Long itemId;
    private Long goodsId;
    private String goodsName;
    private String goodsType;
    private String spec;
    private BigDecimal price;
    private Integer buyNum;
    private BigDecimal amount;
}
