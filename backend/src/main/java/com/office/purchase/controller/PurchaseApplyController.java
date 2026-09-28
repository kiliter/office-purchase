package com.office.purchase.controller;

import com.office.purchase.annotation.RequireRole;
import com.office.purchase.common.Result;
import com.office.purchase.common.RoleConst;
import com.office.purchase.common.UserContext;
import com.office.purchase.dto.ApplySubmitDTO;
import com.office.purchase.dto.AuditDTO;
import com.office.purchase.service.PurchaseApplyService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;

/**
 * 采购申请与审批接口。
 */
@RestController
@RequestMapping("/apply")
public class PurchaseApplyController {

    @Resource
    private PurchaseApplyService purchaseApplyService;

    /**
     * 员工提交新的采购申请。
     */
    @PostMapping("/submit")
    @RequireRole(RoleConst.STAFF)
    public Result<Long> submit(@Valid @RequestBody ApplySubmitDTO dto) {
        Long applyId = purchaseApplyService.submit(dto, UserContext.get().getUserId());
        return Result.ok("采购申请已提交", applyId);
    }

    /**
     * 员工修改被驳回的申请并重新提交。
     */
    @PutMapping("/resubmit")
    @RequireRole(RoleConst.STAFF)
    public Result<Void> resubmit(@Valid @RequestBody ApplySubmitDTO dto) {
        purchaseApplyService.resubmit(dto, UserContext.get().getUserId());
        return Result.ok("申请已重新提交", null);
    }

    /**
     * 分页查询申请。员工只能看到自己的，管理员和审核人员看到全部。
     */
    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1") long current,
                          @RequestParam(defaultValue = "10") long size,
                          @RequestParam(required = false) String auditStatus,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime) {
        return Result.ok(purchaseApplyService.pageApplies(UserContext.get(), current, size, auditStatus, keyword, beginTime, endTime));
    }

    /**
     * 查看申请详情。
     */
    @GetMapping("/{applyId}")
    public Result<?> detail(@PathVariable Long applyId) {
        return Result.ok(purchaseApplyService.detail(applyId, UserContext.get()));
    }

    /**
     * 审核人员通过或驳回申请。通过后自动生成订单。
     */
    @PostMapping("/audit")
    @RequireRole(RoleConst.AUDIT)
    public Result<Void> audit(@Valid @RequestBody AuditDTO dto) {
        purchaseApplyService.audit(dto, UserContext.get().getUserId());
        return Result.ok("审批已保存", null);
    }
}
