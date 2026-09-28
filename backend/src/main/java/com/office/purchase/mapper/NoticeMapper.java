package com.office.purchase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.purchase.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统公告数据访问接口。
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {}
