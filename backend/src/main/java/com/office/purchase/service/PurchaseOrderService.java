package com.office.purchase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.office.purchase.common.PageResult;
import com.office.purchase.entity.PurchaseOrder;
import com.office.purchase.entity.User;
import com.office.purchase.vo.OrderVO;

/**
 * 采购订单查询与状态维护。
 */
public interface PurchaseOrderService extends IService<PurchaseOrder> {

    /**
     * 分页查询订单。普通员工只看自己申请生成的订单。
     */
    PageResult<OrderVO> pageOrders(User current, long currentPage, long size, String orderStatus, String keyword);

    /**
     * 管理员修改订单状态：待采购、采购中、已到货、已完成。
     */
    void updateStatus(Long orderId, String orderStatus);
}
