package com.office.purchase.vo;

import lombok.Data;

/**
 * 登录成功后返回给前端的身份信息。不包含密码。
 */
@Data
public class LoginVO {

    private String token;
    private Long userId;
    private String username;
    private String realName;
    private String role;
    private String phone;
}
