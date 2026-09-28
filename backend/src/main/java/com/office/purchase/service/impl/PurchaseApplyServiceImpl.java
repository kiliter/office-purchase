package com.office.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.purchase.common.BizException;
import com.office.purchase.common.PageResult;
import com.office.purchase.common.RoleConst;
import com.office.purchase.common.StatusConst;
import com.office.purchase.dto.ApplyItemDTO;
import com.office.purchase.dto.ApplySubmitDTO;
import com.office.purchase.dto.AuditDTO;
import com.office.purchase.entity.Goods;
import com.office.purchase.entity.PurchaseApply;
import com.office.purchase.entity.PurchaseApplyItem;
import com.office.purchase.entity.PurchaseOrder;
import com.office.purchase.entity.User;
import com.office.purchase.mapper.GoodsMapper;
import com.office.purchase.mapper.PurchaseApplyItemMapper;
import com.office.purchase.mapper.PurchaseApplyMapper;
import com.office.purchase.mapper.PurchaseOrderMapper;
import com.office.purchase.mapper.UserMapper;
import com.office.purchase.service.PurchaseApplyService;
import com.office.purchase.service.support.ApplyViewAssembler;
import com.office.purchase.vo.ApplyDetailVO;
import com.office.purchase.vo.ApplyItemVO;
import com.office.purchase.vo.ApplyListVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 采购申请提交、驳回后重提，以及审核通过后生成订单。
 */
@Service
public class PurchaseApplyServiceImpl extends ServiceImpl<PurchaseApplyMapper, PurchaseApply>
        implements PurchaseApplyService {

    @Resource
    private PurchaseApplyItemMapper itemMapper;

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private PurchaseOrderMapper orderMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ApplyViewAssembler assembler;

    /**
     * 保存申请主表和明细。明细校验失败时整单回滚。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(ApplySubmitDTO dto, Long userId) {
        List<PurchaseApplyItem> items = buildItems(dto.getItems());
        PurchaseApply apply = new PurchaseApply();
        apply.setApplyUserId(userId);
        apply.setApplyReason(dto.getApplyReason().trim());
        apply.setApplyTime(new Date());
        apply.setAuditStatus(StatusConst.PENDING);
        this.save(apply);
        saveItems(apply.getApplyId(), items);
        return apply.getApplyId();
    }

    /**
     * 只有申请人本人，且状态为已驳回时，才能改单重提。
     * 重提后清空上一次审批人、意见和时间，状态回到待审批。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resubmit(ApplySubmitDTO dto, Long userId) {
        if (dto.getApplyId() == null) {
            throw new BizException("缺少申请编号");
        }
        PurchaseApply apply = this.getById(dto.getApplyId());
        if (apply == null) {
            throw new BizException("采购申请不存在");
        }
        if (!apply.getApplyUserId().equals(userId)) {
            throw new BizException("只能修改自己的采购申请");
        }
        if (!StatusConst.REJECTED.equals(apply.getAuditStatus())) {
            throw new BizException("只有已驳回的申请可以修改后重新提交");
        }
        List<PurchaseApplyItem> items = buildItems(dto.getItems());
        this.update(new LambdaUpdateWrapper<PurchaseApply>()
                .eq(PurchaseApply::getApplyId, apply.getApplyId())
                .set(PurchaseApply::getApplyReason, dto.getApplyReason().trim())
                .set(PurchaseApply::getApplyTime, new Date())
                .set(PurchaseApply::getAuditStatus, StatusConst.PENDING)
                .setSql("audit_user_id = null")
                .setSql("audit_opinion = null")
                .setSql("audit_time = null"));
        itemMapper.delete(new LambdaQueryWrapper<PurchaseApplyItem>()
                .eq(PurchaseApplyItem::getApplyId, apply.getApplyId()));
        saveItems(apply.getApplyId(), items);
    }

    /**
     * 查询申请列表。员工强制限定为自己的单据，关键字可匹配理由或申请人。
     */
    @Override
    public PageResult<ApplyListVO> pageApplies(User current, long currentPage, long size, String auditStatus,
                                               String keyword, Date beginTime, Date endTime) {
        if (currentPage < 1) {
            currentPage = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        LambdaQueryWrapper<PurchaseApply> wrapper = new LambdaQueryWrapper<PurchaseApply>();
        if (RoleConst.STAFF.equals(current.getRole())) {
            wrapper.eq(PurchaseApply::getApplyUserId, current.getUserId());
        }
        if (StringUtils.hasText(auditStatus)) {
            wrapper.eq(PurchaseApply::getAuditStatus, auditStatus.trim());
        }
        if (beginTime != null) {
            wrapper.ge(PurchaseApply::getApplyTime, beginTime);
        }
        if (endTime != null) {
            wrapper.le(PurchaseApply::getApplyTime, endTime);
        }
        applyKeyword(wrapper, keyword);
        wrapper.orderByDesc(PurchaseApply::getApplyTime);
        Page<PurchaseApply> page = this.page(new Page<PurchaseApply>(currentPage, size), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), toList(page.getRecords()));
    }

    /**
     * 读取详情。员工越权查看别人的申请时直接拒绝。
     */
    @Override
    public ApplyDetailVO detail(Long applyId, User current) {
        PurchaseApply apply = this.getById(applyId);
        if (apply == null) {
            throw new BizException("采购申请不存在");
        }
        if (RoleConst.STAFF.equals(current.getRole()) && !apply.getApplyUserId().equals(current.getUserId())) {
            throw new BizException("只能查看自己的采购申请");
        }
        List<ApplyItemVO> items = assembler.itemsOf(applyId);
        Set<Long> userIds = new LinkedHashSet<Long>();
        userIds.add(apply.getApplyUserId());
        if (apply.getAuditUserId() != null) {
            userIds.add(apply.getAuditUserId());
        }
        Map<Long, User> users = assembler.users(userIds);
        ApplyDetailVO vo = new ApplyDetailVO();
        vo.setApplyId(apply.getApplyId());
        vo.setApplyUserId(apply.getApplyUserId());
        vo.setApplyUserName(assembler.nameOf(users.get(apply.getApplyUserId())));
        vo.setApplyReason(apply.getApplyReason());
        vo.setApplyTime(apply.getApplyTime());
        vo.setAuditStatus(apply.getAuditStatus());
        vo.setAuditUserId(apply.getAuditUserId());
        vo.setAuditUserName(apply.getAuditUserId() == null ? "" : assembler.nameOf(users.get(apply.getAuditUserId())));
        vo.setAuditOpinion(apply.getAuditOpinion());
        vo.setAuditTime(apply.getAuditTime());
        vo.setItems(items);
        vo.setGoodsSummary(assembler.summary(items));
        vo.setTotalAmount(assembler.total(items));
        return vo;
    }

    /**
     * 审批只处理待审批单据。通过时在同一事务生成状态为“待采购”的订单。
     * 驳回必须填写意见，员工端靠这个意见修改申请。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(AuditDTO dto, Long auditUserId) {
        if (!StatusConst.PASSED.equals(dto.getAuditStatus()) && !StatusConst.REJECTED.equals(dto.getAuditStatus())) {
            throw new BizException("审批结论只能是已通过或已驳回");
        }
        PurchaseApply apply = this.getById(dto.getApplyId());
        if (apply == null) {
            throw new BizException("采购申请不存在");
        }
        if (!StatusConst.PENDING.equals(apply.getAuditStatus())) {
            throw new BizException("仅待审批的申请可以审批");
        }
        String opinion = dto.getAuditOpinion() == null ? "" : dto.getAuditOpinion().trim();
        if (StatusConst.REJECTED.equals(dto.getAuditStatus()) && !StringUtils.hasText(opinion)) {
            throw new BizException("驳回时必须填写驳回意见");
        }
        if (!StringUtils.hasText(opinion)) {
            opinion = "同意";
        }
        apply.setAuditStatus(dto.getAuditStatus());
        apply.setAuditUserId(auditUserId);
        apply.setAuditOpinion(opinion);
        apply.setAuditTime(new Date());
        this.updateById(apply);
        if (StatusConst.PASSED.equals(dto.getAuditStatus())) {
            long existed = orderMapper.selectCount(new LambdaQueryWrapper<PurchaseOrder>()
                    .eq(PurchaseOrder::getApplyId, apply.getApplyId()));
            if (existed > 0) {
                throw new BizException("该申请已经生成订单");
            }
            PurchaseOrder order = new PurchaseOrder();
            order.setApplyId(apply.getApplyId());
            order.setOrderStatus(StatusConst.ORDER_WAIT);
            order.setCreateTime(new Date());
            orderMapper.insert(order);
        }
    }

    /**
     * 校验商品存在、数量合法，并且同一张申请里不重复选择同一商品。
     */
    private List<PurchaseApplyItem> buildItems(List<ApplyItemDTO> source) {
        List<PurchaseApplyItem> items = new ArrayList<PurchaseApplyItem>();
        Set<Long> seen = new HashSet<Long>();
        for (ApplyItemDTO dto : source) {
            if (!seen.add(dto.getGoodsId())) {
                throw new BizException("同一申请中商品不能重复，请合并数量");
            }
            Goods goods = goodsMapper.selectById(dto.getGoodsId());
            if (goods == null) {
                throw new BizException("选择的商品不存在或已删除");
            }
            PurchaseApplyItem item = new PurchaseApplyItem();
            item.setGoodsId(dto.getGoodsId());
            item.setBuyNum(dto.getBuyNum());
            items.add(item);
        }
        return items;
    }

    /**
     * 写入明细，申请编号在主表保存后才能确定。
     */
    private void saveItems(Long applyId, List<PurchaseApplyItem> items) {
        for (PurchaseApplyItem item : items) {
            item.setApplyId(applyId);
            itemMapper.insert(item);
        }
    }

    /**
     * 关键字同时匹配申请理由，以及申请人的姓名、账号。
     */
    private void applyKeyword(LambdaQueryWrapper<PurchaseApply> wrapper, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String text = keyword.trim();
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .like(User::getRealName, text)
                .or()
                .like(User::getUsername, text));
        final List<Long> userIds = new ArrayList<Long>();
        for (User user : users) {
            userIds.add(user.getUserId());
        }
        wrapper.and(w -> {
            w.like(PurchaseApply::getApplyReason, text);
            if (!userIds.isEmpty()) {
                w.or().in(PurchaseApply::getApplyUserId, userIds);
            }
        });
    }

    /**
     * 主表记录补上姓名、明细摘要和金额。
     */
    private List<ApplyListVO> toList(List<PurchaseApply> records) {
        List<ApplyListVO> result = new ArrayList<ApplyListVO>();
        if (records.isEmpty()) {
            return result;
        }
        List<Long> applyIds = new ArrayList<Long>();
        Set<Long> userIds = new LinkedHashSet<Long>();
        for (PurchaseApply apply : records) {
            applyIds.add(apply.getApplyId());
            userIds.add(apply.getApplyUserId());
            if (apply.getAuditUserId() != null) {
                userIds.add(apply.getAuditUserId());
            }
        }
        Map<Long, List<ApplyItemVO>> itemMap = assembler.itemsGrouped(applyIds);
        Map<Long, User> users = assembler.users(userIds);
        for (PurchaseApply apply : records) {
            List<ApplyItemVO> items = itemMap.get(apply.getApplyId());
            ApplyListVO vo = new ApplyListVO();
            vo.setApplyId(apply.getApplyId());
            vo.setApplyUserId(apply.getApplyUserId());
            vo.setApplyUserName(assembler.nameOf(users.get(apply.getApplyUserId())));
            vo.setApplyReason(apply.getApplyReason());
            vo.setApplyTime(apply.getApplyTime());
            vo.setAuditStatus(apply.getAuditStatus());
            vo.setAuditOpinion(apply.getAuditOpinion());
            vo.setAuditTime(apply.getAuditTime());
            vo.setAuditUserName(apply.getAuditUserId() == null ? "" : assembler.nameOf(users.get(apply.getAuditUserId())));
            vo.setGoodsSummary(assembler.summary(items));
            vo.setTotalAmount(assembler.total(items));
            result.add(vo);
        }
        return result;
    }
}
