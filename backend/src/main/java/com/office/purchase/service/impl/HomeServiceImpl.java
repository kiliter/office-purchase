package com.office.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.office.purchase.common.RoleConst;
import com.office.purchase.common.StatusConst;
import com.office.purchase.entity.Goods;
import com.office.purchase.entity.PurchaseApply;
import com.office.purchase.entity.PurchaseOrder;
import com.office.purchase.entity.User;
import com.office.purchase.mapper.GoodsMapper;
import com.office.purchase.mapper.PurchaseApplyMapper;
import com.office.purchase.mapper.PurchaseOrderMapper;
import com.office.purchase.service.HomeService;
import com.office.purchase.service.NoticeService;
import com.office.purchase.vo.HomeSummaryVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 汇总工作台需要的待办数量。统计口径和各业务列表的数据范围一致。
 */
@Service
public class HomeServiceImpl implements HomeService {

    @Resource
    private PurchaseApplyMapper applyMapper;

    @Resource
    private PurchaseOrderMapper orderMapper;

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private NoticeService noticeService;

    /**
     * 审核人员看全库待审批数，员工看自己的待审批和被驳回数。
     */
    @Override
    public HomeSummaryVO summary(User current) {
        HomeSummaryVO vo = new HomeSummaryVO();
        vo.setRealName(current.getRealName());
        vo.setRole(current.getRole());
        vo.setGoodsCount(goodsMapper.selectCount(new LambdaQueryWrapper<Goods>()));
        vo.setNotices(noticeService.latest(5));
        if (RoleConst.STAFF.equals(current.getRole())) {
            vo.setMyPendingCount(countApply(current.getUserId(), StatusConst.PENDING));
            vo.setMyRejectedCount(countApply(current.getUserId(), StatusConst.REJECTED));
            vo.setOrderCount(countStaffOrders(current.getUserId()));
            vo.setPendingAuditCount(0);
            return vo;
        }
        vo.setPendingAuditCount(applyMapper.selectCount(new LambdaQueryWrapper<PurchaseApply>()
                .eq(PurchaseApply::getAuditStatus, StatusConst.PENDING)));
        vo.setOrderCount(orderMapper.selectCount(new LambdaQueryWrapper<PurchaseOrder>()));
        vo.setMyPendingCount(0);
        vo.setMyRejectedCount(0);
        return vo;
    }

    /**
     * 统计某员工某种状态的申请数量。
     */
    private long countApply(Long userId, String status) {
        return applyMapper.selectCount(new LambdaQueryWrapper<PurchaseApply>()
                .eq(PurchaseApply::getApplyUserId, userId)
                .eq(PurchaseApply::getAuditStatus, status));
    }

    /**
     * 员工订单数等于其申请已生成的订单数。
     */
    private long countStaffOrders(Long userId) {
        List<PurchaseApply> applies = applyMapper.selectList(new LambdaQueryWrapper<PurchaseApply>()
                .eq(PurchaseApply::getApplyUserId, userId));
        if (applies.isEmpty()) {
            return 0;
        }
        List<Long> ids = new java.util.ArrayList<Long>();
        for (PurchaseApply apply : applies) {
            ids.add(apply.getApplyId());
        }
        return orderMapper.selectCount(new LambdaQueryWrapper<PurchaseOrder>().in(PurchaseOrder::getApplyId, ids));
    }
}
