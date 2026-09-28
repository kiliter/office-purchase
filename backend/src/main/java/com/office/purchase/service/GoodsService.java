package com.office.purchase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.office.purchase.common.PageResult;
import com.office.purchase.dto.GoodsSaveDTO;
import com.office.purchase.entity.Goods;

/**
 * 办公用品商品维护。员工只有查询权限，写操作在控制器上限制为管理员。
 */
public interface GoodsService extends IService<Goods> {

    /**
     * 按名称、类别分页查询商品。
     */
    PageResult<Goods> pageGoods(long current, long size, String goodsName, String goodsType);

    /**
     * 录入新的办公用品。
     */
    void createGoods(GoodsSaveDTO dto);

    /**
     * 修改名称、规格、单价和库存。
     */
    void updateGoods(GoodsSaveDTO dto);

    /**
     * 删除未被采购申请引用的商品。
     */
    void deleteGoods(Long goodsId);
}
