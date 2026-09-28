package com.office.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.purchase.common.BizException;
import com.office.purchase.common.PageResult;
import com.office.purchase.common.RoleConst;
import com.office.purchase.common.StatusConst;
import com.office.purchase.entity.PurchaseApply;
import com.office.purchase.entity.PurchaseOrder;
import com.office.purchase.entity.User;
import com.office.purchase.mapper.PurchaseApplyMapper;
import com.office.purchase.mapper.PurchaseOrderMapper;
import com.office.purchase.mapper.UserMapper;
import com.office.purchase.service.PurchaseOrderService;
import com.office.purchase.service.support.ApplyViewAssembler;
import com.office.purchase.vo.ApplyItemVO;
import com.office.purchase.vo.OrderVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 订单由审批通过自动生成，这里负责查询和履约状态更新。
 */
@Service
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder>
        implements PurchaseOrderService {

    @Resource
    private PurchaseApplyMapper applyMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ApplyViewAssembler assembler;

    /**
     * 员工只能看到自己申请产生的订单；管理员和审核人员查看全部。
     */
    @Override
    public PageResult<OrderVO> pageOrders(User current, long currentPage, long size, String orderStatus, String keyword) {
        if (currentPage < 1) {
            currentPage = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<PurchaseOrder>();
        List<Long> scopedApplyIds = null;
        if (RoleConst.STAFF.equals(current.getRole())) {
            scopedApplyIds = applyIdsOfUser(current.getUserId());
            if (scopedApplyIds.isEmpty()) {
                return PageResult.empty(currentPage, size);
            }
            wrapper.in(PurchaseOrder::getApplyId, scopedApplyIds);
        }
        if (StringUtils.hasText(orderStatus)) {
            wrapper.eq(PurchaseOrder::getOrderStatus, orderStatus.trim());
        }
        if (StringUtils.hasText(keyword)) {
            List<Long> matched = matchApplyIds(keyword.trim(), scopedApplyIds);
            if (matched.isEmpty()) {
                return PageResult.empty(currentPage, size);
            }
            wrapper.in(PurchaseOrder::getApplyId, matched);
        }
        wrapper.orderByDesc(PurchaseOrder::getCreateTime);
        Page<PurchaseOrder> page = this.page(new Page<PurchaseOrder>(currentPage, size), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), toList(page.getRecords()));
    }

    /**
     * 只允许改成论文规定的四种订单状态。
     */
    @Override
    public void updateStatus(Long orderId, String orderStatus) {
        if (!StatusConst.ORDER_STATUSES.contains(orderStatus)) {
            throw new BizException("订单状态只能是待采购、采购中、已到货或已完成");
        }
        PurchaseOrder order = this.getById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        order.setOrderStatus(orderStatus);
        this.updateById(order);
    }

    /**
     * 找出某个员工名下的全部申请编号。
     */
    private List<Long> applyIdsOfUser(Long userId) {
        List<PurchaseApply> applies = applyMapper.selectList(new LambdaQueryWrapper<PurchaseApply>()
                .eq(PurchaseApply::getApplyUserId, userId));
        List<Long> ids = new ArrayList<Long>();
        for (PurchaseApply apply : applies) {
            ids.add(apply.getApplyId());
        }
        return ids;
    }

    /**
     * 按申请人姓名、账号或申请理由找出申请编号。员工查询再限制在本人申请内。
     */
    private List<Long> matchApplyIds(String keyword, List<Long> scopedApplyIds) {
        List<User> matchedUsers = userMapper.selectList(new LambdaQueryWrapper<User>()
                .like(User::getRealName, keyword)
                .or()
                .like(User::getUsername, keyword));
        final List<Long> userIds = new ArrayList<Long>();
        for (User user : matchedUsers) {
            userIds.add(user.getUserId());
        }
        LambdaQueryWrapper<PurchaseApply> wrapper = new LambdaQueryWrapper<PurchaseApply>();
        if (scopedApplyIds != null) {
            wrapper.in(PurchaseApply::getApplyId, scopedApplyIds);
        }
        wrapper.and(w -> {
            w.like(PurchaseApply::getApplyReason, keyword);
            if (!userIds.isEmpty()) {
                w.or().in(PurchaseApply::getApplyUserId, userIds);
            }
        });
        List<PurchaseApply> applies = applyMapper.selectList(wrapper);
        List<Long> ids = new ArrayList<Long>();
        for (PurchaseApply apply : applies) {
            ids.add(apply.getApplyId());
        }
        return ids;
    }

    /**
     * 订单补上对应申请的人和明细。
     */
    private List<OrderVO> toList(List<PurchaseOrder> records) {
        List<OrderVO> result = new ArrayList<OrderVO>();
        if (records.isEmpty()) {
            return result;
        }
        List<Long> applyIds = new ArrayList<Long>();
        for (PurchaseOrder order : records) {
            applyIds.add(order.getApplyId());
        }
        List<PurchaseApply> applies = applyMapper.selectBatchIds(applyIds);
        Map<Long, PurchaseApply> applyMap = new HashMap<Long, PurchaseApply>();
        Set<Long> userIds = new LinkedHashSet<Long>();
        for (PurchaseApply apply : applies) {
            applyMap.put(apply.getApplyId(), apply);
            userIds.add(apply.getApplyUserId());
        }
        Map<Long, User> users = assembler.users(userIds);
        Map<Long, List<ApplyItemVO>> items = assembler.itemsGrouped(applyIds);
        for (PurchaseOrder order : records) {
            PurchaseApply apply = applyMap.get(order.getApplyId());
            List<ApplyItemVO> itemList = items.get(order.getApplyId());
            OrderVO vo = new OrderVO();
            vo.setOrderId(order.getOrderId());
            vo.setApplyId(order.getApplyId());
            vo.setOrderStatus(order.getOrderStatus());
            vo.setCreateTime(order.getCreateTime());
            if (apply != null) {
                vo.setApplyUserId(apply.getApplyUserId());
                vo.setApplyUserName(assembler.nameOf(users.get(apply.getApplyUserId())));
                vo.setApplyReason(apply.getApplyReason());
            }
            vo.setItems(itemList);
            vo.setGoodsSummary(assembler.summary(itemList));
            vo.setTotalAmount(assembler.total(itemList));
            result.add(vo);
        }
        return result;
    }
}
