package com.office.purchase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.office.purchase.common.PageResult;
import com.office.purchase.dto.PasswordDTO;
import com.office.purchase.dto.UserCreateDTO;
import com.office.purchase.dto.UserUpdateDTO;
import com.office.purchase.entity.User;
import com.office.purchase.vo.LoginVO;

/**
 * 用户与登录业务。
 */
public interface UserService extends IService<User> {

    /**
     * 校验账号密码，成功后签发登录令牌。
     */
    LoginVO login(String username, String password);

    /**
     * 按账号、姓名、角色分页查询用户。
     */
    PageResult<User> pageUsers(long current, long size, String username, String realName, String role);

    /**
     * 管理员新增用户，密码加密后入库。
     */
    void createUser(UserCreateDTO dto);

    /**
     * 管理员修改姓名、手机号和角色。
     */
    void updateUser(UserUpdateDTO dto, Long operatorId);

    /**
     * 删除没有业务单据的用户。不能删除自己。
     */
    void deleteUser(Long userId, Long operatorId);

    /**
     * 把指定用户的密码重置为 123456。
     */
    void resetPassword(Long userId);

    /**
     * 当前用户校验原密码后修改新密码。
     */
    void changePassword(Long userId, PasswordDTO dto);
}
