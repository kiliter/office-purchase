package com.office.purchase.service.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.office.purchase.entity.Goods;
import com.office.purchase.entity.PurchaseApplyItem;
import com.office.purchase.entity.User;
import com.office.purchase.mapper.GoodsMapper;
import com.office.purchase.mapper.PurchaseApplyItemMapper;
import com.office.purchase.mapper.UserMapper;
import com.office.purchase.vo.ApplyItemVO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 把申请明细、商品和用户组装成页面需要的名称与金额。
 * 金额使用商品当前单价，因为明细表按论文设计不保存成交价快照。
 */
@Component
public class ApplyViewAssembler {

    @Resource
    private PurchaseApplyItemMapper itemMapper;

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 按用户编号批量取出姓名，避免列表里逐条查询。
     */
    public Map<Long, User> users(Collection<Long> userIds) {
        Map<Long, User> map = new HashMap<Long, User>();
        if (userIds == null || userIds.isEmpty()) {
            return map;
        }
        List<User> users = userMapper.selectBatchIds(userIds);
        for (User user : users) {
            map.put(user.getUserId(), user);
        }
        return map;
    }

    /**
     * 一次查出多张申请的明细，并按申请编号分组。
     */
    public Map<Long, List<ApplyItemVO>> itemsGrouped(Collection<Long> applyIds) {
        Map<Long, List<ApplyItemVO>> grouped = new HashMap<Long, List<ApplyItemVO>>();
        if (applyIds == null || applyIds.isEmpty()) {
            return grouped;
        }
        List<PurchaseApplyItem> items = itemMapper.selectList(new LambdaQueryWrapper<PurchaseApplyItem>()
                .in(PurchaseApplyItem::getApplyId, applyIds)
                .orderByAsc(PurchaseApplyItem::getItemId));
        Map<Long, Goods> goodsMap = goodsMap(items);
        for (PurchaseApplyItem item : items) {
            List<ApplyItemVO> list = grouped.get(item.getApplyId());
            if (list == null) {
                list = new ArrayList<ApplyItemVO>();
                grouped.put(item.getApplyId(), list);
            }
            list.add(toItemVo(item, goodsMap.get(item.getGoodsId())));
        }
        return grouped;
    }

    /**
     * 查询单张申请的明细。
     */
    public List<ApplyItemVO> itemsOf(Long applyId) {
        Map<Long, List<ApplyItemVO>> grouped = itemsGrouped(Collections.singletonList(applyId));
        List<ApplyItemVO> items = grouped.get(applyId);
        return items == null ? new ArrayList<ApplyItemVO>() : items;
    }

    /**
     * 把明细收成“商品名×数量”的一句话。
     */
    public String summary(List<ApplyItemVO> items) {
        if (items == null || items.isEmpty()) {
            return "无明细";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                builder.append("、");
            }
            ApplyItemVO item = items.get(i);
            builder.append(item.getGoodsName()).append("×").append(item.getBuyNum());
        }
        return builder.toString();
    }

    /**
     * 汇总明细金额，保留两位小数。
     */
    public BigDecimal total(List<ApplyItemVO> items) {
        BigDecimal total = BigDecimal.ZERO;
        if (items != null) {
            for (ApplyItemVO item : items) {
                if (item.getAmount() != null) {
                    total = total.add(item.getAmount());
                }
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 用户被删除或不存在时，列表仍要有可显示的名字。
     */
    public String nameOf(User user) {
        return user == null ? "未知用户" : user.getRealName();
    }

    /**
     * 按明细里的商品编号取出商品。商品已删除时映射里没有对应项。
     */
    private Map<Long, Goods> goodsMap(List<PurchaseApplyItem> items) {
        Map<Long, Goods> map = new HashMap<Long, Goods>();
        List<Long> ids = new ArrayList<Long>();
        for (PurchaseApplyItem item : items) {
            if (!ids.contains(item.getGoodsId())) {
                ids.add(item.getGoodsId());
            }
        }
        if (ids.isEmpty()) {
            return map;
        }
        List<Goods> goodsList = goodsMapper.selectBatchIds(ids);
        for (Goods goods : goodsList) {
            map.put(goods.getGoodsId(), goods);
        }
        return map;
    }

    /**
     * 明细行转展示对象。商品不存在时金额按 0 计算，并标明已删除。
     */
    private ApplyItemVO toItemVo(PurchaseApplyItem item, Goods goods) {
        ApplyItemVO vo = new ApplyItemVO();
        vo.setItemId(item.getItemId());
        vo.setGoodsId(item.getGoodsId());
        vo.setBuyNum(item.getBuyNum());
        if (goods == null) {
            vo.setGoodsName("商品已删除");
            vo.setGoodsType("");
            vo.setSpec("");
            vo.setPrice(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            vo.setAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            return vo;
        }
        vo.setGoodsName(goods.getGoodsName());
        vo.setGoodsType(goods.getGoodsType());
        vo.setSpec(goods.getSpec());
        vo.setPrice(goods.getPrice());
        vo.setAmount(goods.getPrice().multiply(new BigDecimal(item.getBuyNum())).setScale(2, RoundingMode.HALF_UP));
        return vo;
    }
}
