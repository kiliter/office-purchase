package com.office.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.purchase.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问接口。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {}
