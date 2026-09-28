package com.office.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 采购申请主表。一条申请可包含多条商品明细。
 */
@Data
@TableName("purchase_apply")
public class PurchaseApply {

    @TableId(value = "apply_id", type = IdType.AUTO)
    private Long applyId;

    private Long applyUserId;

    private String applyReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date applyTime;

    /** 待审批 / 已通过 / 已驳回 */
    private String auditStatus;

    private Long auditUserId;

    private String auditOpinion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date auditTime;
}
