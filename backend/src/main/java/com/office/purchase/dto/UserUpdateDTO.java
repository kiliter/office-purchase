package com.office.purchase.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 管理员修改用户资料。账号创建后不可改，避免历史单据对不上人。
 */
@Data
public class UserUpdateDTO {

    @NotNull(message = "缺少用户编号")
    private Long userId;

    @NotBlank(message = "请输入姓名")
    @Size(max = 50, message = "姓名不能超过 50 个字")
    private String realName;

    @NotBlank(message = "请选择角色")
    private String role;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "请输入 11 位手机号")
    private String phone;
}
