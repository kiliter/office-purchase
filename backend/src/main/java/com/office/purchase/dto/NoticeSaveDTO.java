package com.office.purchase.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 发布或修改公告。修改时带 noticeId。
 */
@Data
public class NoticeSaveDTO {

    private Long noticeId;

    @NotBlank(message = "请输入公告标题")
    @Size(max = 100, message = "标题不能超过 100 个字")
    private String title;

    @NotBlank(message = "请输入公告内容")
    private String content;
}
