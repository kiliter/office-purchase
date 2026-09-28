package com.office.purchase.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求。只接收账号和密码。
 */
@Data
public class LoginDTO {

    @NotBlank(message = "请输入账号")
    private String username;

    @NotBlank(message = "请输入密码")
    private String password;
}
