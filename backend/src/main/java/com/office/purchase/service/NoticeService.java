package com.office.purchase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.office.purchase.common.PageResult;
import com.office.purchase.dto.NoticeSaveDTO;
import com.office.purchase.entity.Notice;

import java.util.List;

/**
 * 系统公告。
 */
public interface NoticeService extends IService<Notice> {

    /**
     * 按标题分页查询公告，新发布的排在前面。
     */
    PageResult<Notice> pageNotices(long current, long size, String title);

    /**
     * 取最近几条公告，供工作台展示。
     */
    List<Notice> latest(int limit);

    /**
     * 发布公告。
     */
    void createNotice(NoticeSaveDTO dto);

    /**
     * 修改公告标题和内容，发布时间更新为当前时间。
     */
    void updateNotice(NoticeSaveDTO dto);

    /**
     * 删除公告。
     */
    void deleteNotice(Long noticeId);
}
