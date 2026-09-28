package com.office.purchase.common;

import java.util.Arrays;
import java.util.List;

/**
 * 采购申请和订单使用的中文状态，和论文表设计保持一致。
 */
public final class StatusConst {

    public static final String PENDING = "待审批";
    public static final String PASSED = "已通过";
    public static final String REJECTED = "已驳回";

    public static final String ORDER_WAIT = "待采购";
    public static final String ORDER_DOING = "采购中";
    public static final String ORDER_ARRIVED = "已到货";
    public static final String ORDER_DONE = "已完成";

    /** 管理员允许写入的订单状态 */
    public static final List<String> ORDER_STATUSES = Arrays.asList(
            ORDER_WAIT, ORDER_DOING, ORDER_ARRIVED, ORDER_DONE);

    private StatusConst() {
    }
}
