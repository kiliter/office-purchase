package com.office.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.purchase.common.BizException;
import com.office.purchase.common.PageResult;
import com.office.purchase.dto.GoodsSaveDTO;
import com.office.purchase.entity.Goods;
import com.office.purchase.entity.PurchaseApplyItem;
import com.office.purchase.mapper.GoodsMapper;
import com.office.purchase.mapper.PurchaseApplyItemMapper;
import com.office.purchase.service.GoodsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.RoundingMode;
import java.util.Date;

/**
 * 办公用品档案维护。库存由管理员手工调整，审批通过不会自动扣减。
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Resource
    private PurchaseApplyItemMapper itemMapper;

    /**
     * 商品分页。名称和类别都按包含关系筛选。
     */
    @Override
    public PageResult<Goods> pageGoods(long current, long size, String goodsName, String goodsType) {
        if (current < 1) {
            current = 1;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 500) {
            size = 500;
        }
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<Goods>();
        if (StringUtils.hasText(goodsName)) {
            wrapper.like(Goods::getGoodsName, goodsName.trim());
        }
        if (StringUtils.hasText(goodsType)) {
            wrapper.like(Goods::getGoodsType, goodsType.trim());
        }
        wrapper.orderByDesc(Goods::getCreateTime);
        Page<Goods> page = this.page(new Page<Goods>(current, size), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /**
     * 录入商品，单价统一保留两位小数。
     */
    @Override
    public void createGoods(GoodsSaveDTO dto) {
        Goods goods = new Goods();
        fill(goods, dto);
        goods.setCreateTime(new Date());
        this.save(goods);
    }

    /**
     * 修改已有商品。不改录入时间，方便区分新增和后来的调价。
     */
    @Override
    public void updateGoods(GoodsSaveDTO dto) {
        if (dto.getGoodsId() == null) {
            throw new BizException("缺少商品编号");
        }
        Goods goods = this.getById(dto.getGoodsId());
        if (goods == null) {
            throw new BizException("商品不存在");
        }
        fill(goods, dto);
        this.updateById(goods);
    }

    /**
     * 已被申请引用的商品保留档案，避免历史明细失去名称。
     */
    @Override
    public void deleteGoods(Long goodsId) {
        Goods goods = this.getById(goodsId);
        if (goods == null) {
            throw new BizException("商品不存在");
        }
        long used = itemMapper.selectCount(new LambdaQueryWrapper<PurchaseApplyItem>()
                .eq(PurchaseApplyItem::getGoodsId, goodsId));
        if (used > 0) {
            throw new BizException("该商品已被采购申请引用，不能删除");
        }
        this.removeById(goodsId);
    }

    /**
     * 把表单写入商品实体。规格允许为空。
     */
    private void fill(Goods goods, GoodsSaveDTO dto) {
        goods.setGoodsName(dto.getGoodsName().trim());
        goods.setGoodsType(dto.getGoodsType().trim());
        goods.setSpec(StringUtils.hasText(dto.getSpec()) ? dto.getSpec().trim() : "");
        goods.setPrice(dto.getPrice().setScale(2, RoundingMode.HALF_UP));
        goods.setStock(dto.getStock());
    }
}
