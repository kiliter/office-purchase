package com.office.purchase.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 提交或重新提交采购申请。重新提交时带上原申请编号。
 */
@Data
public class ApplySubmitDTO {

    /** 重新提交时必填，新申请留空 */
    private Long applyId;

    @NotBlank(message = "请填写申请理由")
    @Size(max = 500, message = "申请理由不能超过 500 个字")
    private String applyReason;

    @NotEmpty(message = "请至少选择一种商品")
    @Valid
    private List<ApplyItemDTO> items;
}
