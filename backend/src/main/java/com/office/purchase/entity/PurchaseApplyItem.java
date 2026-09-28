package com.office.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 采购申请明细，记录本次申请的商品和数量。
 */
@Data
@TableName("purchase_apply_item")
public class PurchaseApplyItem {

    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;

    private Long applyId;

    private Long goodsId;

    /** 申请采购数量 */
    private Integer buyNum;
}
