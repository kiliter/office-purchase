package com.office.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 采购订单。审核通过时按申请单一对一生成。
 */
@Data
@TableName("purchase_order")
public class PurchaseOrder {

    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    private Long applyId;

    /** 待采购 / 采购中 / 已到货 / 已完成 */
    private String orderStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createTime;
}
