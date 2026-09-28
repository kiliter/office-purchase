package com.office.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

/**
 * 用户信息，对应 user 表。
 */
@Data
@TableName("`user`")
public class User {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /** 登录账号，全局唯一 */
    private String username;

    /** 加密后的密码，接口响应中不返回 */
    @JsonIgnore
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 角色：admin / audit / staff */
    private String role;

    /** 手机号，可空 */
    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createTime;
}
