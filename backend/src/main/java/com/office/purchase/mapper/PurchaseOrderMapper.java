package com.office.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.purchase.entity.PurchaseOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购订单数据访问接口。
 */
@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {}
