package com.office.purchase.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 采购申请列表行。明细收成一句话，避免表格过宽。
 */
@Data
public class ApplyListVO {

    private Long applyId;
    private Long applyUserId;
    private String applyUserName;
    private String applyReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date applyTime;

    private String auditStatus;
    private String auditOpinion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date auditTime;

    private String auditUserName;
    private String goodsSummary;
    private BigDecimal totalAmount;
}
