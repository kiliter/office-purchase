package com.office.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.purchase.common.BizException;
import com.office.purchase.common.PageResult;
import com.office.purchase.common.RoleConst;
import com.office.purchase.dto.PasswordDTO;
import com.office.purchase.dto.UserCreateDTO;
import com.office.purchase.dto.UserUpdateDTO;
import com.office.purchase.entity.PurchaseApply;
import com.office.purchase.entity.User;
import com.office.purchase.mapper.PurchaseApplyMapper;
import com.office.purchase.mapper.UserMapper;
import com.office.purchase.service.UserService;
import com.office.purchase.util.JwtUtil;
import com.office.purchase.vo.LoginVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 用户登录、资料维护和密码处理。
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /** 管理员重置密码时使用的初始口令，与论文测试账号一致 */
    public static final String DEFAULT_PASSWORD = "123456";

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private PurchaseApplyMapper purchaseApplyMapper;

    /**
     * 账号不存在或密码不匹配时，统一提示，避免暴露账号是否存在。
     */
    @Override
    public LoginVO login(String username, String password) {
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BizException("账号或者密码错误");
        }
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.createToken(user));
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setPhone(user.getPhone());
        return vo;
    }

    /**
     * 管理端用户列表，支持账号、姓名模糊查询和角色精确筛选。
     */
    @Override
    public PageResult<User> pageUsers(long current, long size, String username, String realName, String role) {
        long[] pageArgs = normalize(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>();
        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, username.trim());
        }
        if (StringUtils.hasText(realName)) {
            wrapper.like(User::getRealName, realName.trim());
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role.trim());
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> page = this.page(new Page<User>(pageArgs[0], pageArgs[1]), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /**
     * 新增用户前检查账号重复和角色合法性，密码只存密文。
     */
    @Override
    public void createUser(UserCreateDTO dto) {
        checkRole(dto.getRole());
        long exists = this.count(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername().trim()));
        if (exists > 0) {
            throw new BizException("账号已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName().trim());
        user.setRole(dto.getRole());
        user.setPhone(blankToNull(dto.getPhone()));
        user.setCreateTime(new Date());
        this.save(user);
    }

    /**
     * 修改用户资料。操作者不能取消自己的管理员身份，否则系统会没有管理员。
     */
    @Override
    public void updateUser(UserUpdateDTO dto, Long operatorId) {
        checkRole(dto.getRole());
        User user = this.getById(dto.getUserId());
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (user.getUserId().equals(operatorId) && !RoleConst.ADMIN.equals(dto.getRole())) {
            throw new BizException("不能取消自己的管理员角色");
        }
        user.setRealName(dto.getRealName().trim());
        user.setRole(dto.getRole());
        user.setPhone(blankToNull(dto.getPhone()));
        this.updateById(user);
    }

    /**
     * 有申请记录的用户不能删除，否则历史单据会失去申请人。
     */
    @Override
    public void deleteUser(Long userId, Long operatorId) {
        if (userId.equals(operatorId)) {
            throw new BizException("不能删除当前登录账号");
        }
        User user = this.getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        long related = purchaseApplyMapper.selectCount(new LambdaQueryWrapper<PurchaseApply>()
                .eq(PurchaseApply::getApplyUserId, userId)
                .or()
                .eq(PurchaseApply::getAuditUserId, userId));
        if (related > 0) {
            throw new BizException("该用户已有采购申请或审批记录，不能删除");
        }
        this.removeById(userId);
    }

    /**
     * 重置为论文中的初始密码 123456。
     */
    @Override
    public void resetPassword(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        this.updateById(user);
    }

    /**
     * 修改自己的密码。新密码不能与原密码相同。
     */
    @Override
    public void changePassword(Long userId, PasswordDTO dto) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new BizException("新密码不能与原密码相同");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        this.updateById(user);
    }

    /**
     * 角色只能是管理员、审核人员、普通员工三种。
     */
    private void checkRole(String role) {
        if (!RoleConst.valid(role)) {
            throw new BizException("角色只能是管理员、审核人员或普通员工");
        }
    }

    /**
     * 空白手机号按未填写处理。
     */
    private String blankToNull(String phone) {
        if (!StringUtils.hasText(phone)) {
            return "";
        }
        return phone.trim();
    }

    /**
     * 限制分页参数，避免一次拉取过多数据。
     */
    private long[] normalize(long current, long size) {
        if (current < 1) {
            current = 1;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }
        return new long[]{current, size};
    }
}
