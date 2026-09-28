package com.office.purchase.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 采购申请详情，供查看、审批和重新提交前回显。
 */
@Data
public class ApplyDetailVO {

    private Long applyId;
    private Long applyUserId;
    private String applyUserName;
    private String applyReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date applyTime;

    private String auditStatus;
    private Long auditUserId;
    private String auditUserName;
    private String auditOpinion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date auditTime;

    private String goodsSummary;
    private BigDecimal totalAmount;
    private List<ApplyItemVO> items;
}
