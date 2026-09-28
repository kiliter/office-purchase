package com.office.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 办公用品商品，对应 goods 表。
 */
@Data
@TableName("goods")
public class Goods {

    @TableId(value = "goods_id", type = IdType.AUTO)
    private Long goodsId;

    private String goodsName;

    /** 商品类别，例如纸张文具、书写工具 */
    private String goodsType;

    /** 规格，可空 */
    private String spec;

    private BigDecimal price;

    /** 当前库存。本版本审批通过后不自动扣减，由管理员维护 */
    private Integer stock;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createTime;
}
