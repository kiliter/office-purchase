package com.office.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.purchase.entity.PurchaseApplyItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购申请明细数据访问接口。
 */
@Mapper
public interface PurchaseApplyItemMapper extends BaseMapper<PurchaseApplyItem> {}
