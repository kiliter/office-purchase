package com.office.purchase.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 采购订单展示对象，补上申请人、明细和金额。
 */
@Data
public class OrderVO {

    private Long orderId;
    private Long applyId;
    private String orderStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createTime;

    private Long applyUserId;
    private String applyUserName;
    private String applyReason;
    private String goodsSummary;
    private BigDecimal totalAmount;
    private List<ApplyItemVO> items;
}
