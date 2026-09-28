package com.office.purchase.service;

import com.office.purchase.entity.User;
import com.office.purchase.vo.HomeSummaryVO;

/**
 * 工作台摘要。
 */
public interface HomeService {

    /**
     * 按角色统计待办数量，并附上最新公告。
     */
    HomeSummaryVO summary(User current);
}
