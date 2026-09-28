package com.office.purchase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.office.purchase.common.PageResult;
import com.office.purchase.dto.ApplySubmitDTO;
import com.office.purchase.dto.AuditDTO;
import com.office.purchase.entity.PurchaseApply;
import com.office.purchase.entity.User;
import com.office.purchase.vo.ApplyDetailVO;
import com.office.purchase.vo.ApplyListVO;

import java.util.Date;

/**
 * 采购申请与审批。
 */
public interface PurchaseApplyService extends IService<PurchaseApply> {

    /**
     * 员工提交新申请，状态置为待审批。
     */
    Long submit(ApplySubmitDTO dto, Long userId);

    /**
     * 员工修改被驳回的申请并重新提交。
     */
    void resubmit(ApplySubmitDTO dto, Long userId);

    /**
     * 分页查询申请。普通员工只能看到自己的记录。
     */
    PageResult<ApplyListVO> pageApplies(User current, long currentPage, long size, String auditStatus,
                                        String keyword, Date beginTime, Date endTime);

    /**
     * 查看申请详情。普通员工不能查看别人的申请。
     */
    ApplyDetailVO detail(Long applyId, User current);

    /**
     * 审核人员通过或驳回。通过时同时生成采购订单。
     */
    void audit(AuditDTO dto, Long auditUserId);
}
