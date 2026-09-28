package com.office.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.purchase.entity.Goods;
import org.apache.ibatis.annotations.Mapper;

/**
 * 办公用品商品数据访问接口。
 * 继承 BaseMapper 后具备基础增删改查，无需手写 SQL。
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
}
