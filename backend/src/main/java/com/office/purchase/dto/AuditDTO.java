package com.office.purchase.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 审核人员对一条待审批申请给出的结论。
 */
@Data
public class AuditDTO {

    @NotNull(message = "缺少申请编号")
    private Long applyId;

    /** 只接受“已通过”或“已驳回” */
    @NotBlank(message = "请选择审批结论")
    private String auditStatus;

    @Size(max = 500, message = "审批意见不能超过 500 个字")
    private String auditOpinion;
}
