package com.office.purchase.controller;

import com.office.purchase.annotation.RequireRole;
import com.office.purchase.common.Result;
import com.office.purchase.common.RoleConst;
import com.office.purchase.common.UserContext;
import com.office.purchase.dto.LoginDTO;
import com.office.purchase.dto.PasswordDTO;
import com.office.purchase.dto.UserCreateDTO;
import com.office.purchase.dto.UserUpdateDTO;
import com.office.purchase.entity.User;
import com.office.purchase.service.UserService;
import com.office.purchase.vo.LoginVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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

/**
 * 登录与用户管理接口。
 */
@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户登录。账号或密码错误时返回失败说明。
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginUser) {
        LoginVO vo = userService.login(loginUser.getUsername().trim(), loginUser.getPassword());
        return Result.ok("登录成功", vo);
    }

    /**
     * 读取当前登录人，供页面刷新后恢复姓名和角色。
     */
    @GetMapping("/me")
    public Result<User> me() {
        return Result.ok(UserContext.get());
    }

    /**
     * 管理员分页查询用户。
     */
    @GetMapping("/page")
    @RequireRole(RoleConst.ADMIN)
    public Result<?> page(@RequestParam(defaultValue = "1") long current,
                          @RequestParam(defaultValue = "10") long size,
                          @RequestParam(required = false) String username,
                          @RequestParam(required = false) String realName,
                          @RequestParam(required = false) String role) {
        return Result.ok(userService.pageUsers(current, size, username, realName, role));
    }

    /**
     * 管理员新增用户并分配角色。
     */
    @PostMapping
    @RequireRole(RoleConst.ADMIN)
    public Result<Void> create(@Valid @RequestBody UserCreateDTO dto) {
        userService.createUser(dto);
        return Result.ok("用户已新增", null);
    }

    /**
     * 管理员修改用户资料。
     */
    @PutMapping
    @RequireRole(RoleConst.ADMIN)
    public Result<Void> update(@Valid @RequestBody UserUpdateDTO dto) {
        userService.updateUser(dto, UserContext.get().getUserId());
        return Result.ok("用户已保存", null);
    }

    /**
     * 管理员删除用户。
     */
    @DeleteMapping("/{userId}")
    @RequireRole(RoleConst.ADMIN)
    public Result<Void> delete(@PathVariable Long userId) {
        userService.deleteUser(userId, UserContext.get().getUserId());
        return Result.ok("用户已删除", null);
    }

    /**
     * 管理员把用户密码重置为 123456。
     */
    @PutMapping("/reset/{userId}")
    @RequireRole(RoleConst.ADMIN)
    public Result<Void> reset(@PathVariable Long userId) {
        userService.resetPassword(userId);
        return Result.ok("密码已重置为 123456", null);
    }

    /**
     * 当前用户修改自己的密码。
     */
    @PutMapping("/password")
    public Result<Void> password(@Valid @RequestBody PasswordDTO dto) {
        userService.changePassword(UserContext.get().getUserId(), dto);
        return Result.ok("密码已修改，请重新登录", null);
    }
}
