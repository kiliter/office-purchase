package com.office.purchase.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 管理员新增用户。
 */
@Data
public class UserCreateDTO {

    @NotBlank(message = "请输入账号")
    @Size(min = 3, max = 20, message = "账号长度需为 3 到 20 位")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]{2,19}$", message = "账号需以字母开头，只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "请输入初始密码")
    @Size(min = 6, max = 20, message = "密码长度需为 6 到 20 位")
    private String password;

    @NotBlank(message = "请输入姓名")
    @Size(max = 50, message = "姓名不能超过 50 个字")
    private String realName;

    @NotBlank(message = "请选择角色")
    private String role;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "请输入 11 位手机号")
    private String phone;
}
