package com.office.purchase.vo;

import com.office.purchase.entity.Notice;
import lombok.Data;

import java.util.List;

/**
 * 工作台摘要。数量按当前角色的数据范围统计。
 */
@Data
public class HomeSummaryVO {

    private String realName;
    private String role;
    private long pendingAuditCount;
    private long myPendingCount;
    private long myRejectedCount;
    private long orderCount;
    private long goodsCount;
    private List<Notice> notices;
}
