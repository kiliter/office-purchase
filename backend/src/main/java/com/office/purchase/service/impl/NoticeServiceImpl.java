package com.office.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.purchase.common.BizException;
import com.office.purchase.common.PageResult;
import com.office.purchase.dto.NoticeSaveDTO;
import com.office.purchase.entity.Notice;
import com.office.purchase.mapper.NoticeMapper;
import com.office.purchase.service.NoticeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 公告的发布、修改和查询。
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    /**
     * 公告分页，标题按包含关系筛选。
     */
    @Override
    public PageResult<Notice> pageNotices(long current, long size, String title) {
        if (current < 1) {
            current = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<Notice>();
        if (StringUtils.hasText(title)) {
            wrapper.like(Notice::getTitle, title.trim());
        }
        wrapper.orderByDesc(Notice::getPublishTime);
        Page<Notice> page = this.page(new Page<Notice>(current, size), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    /**
     * 取最新公告。工作台默认展示 5 条。
     */
    @Override
    public List<Notice> latest(int limit) {
        if (limit < 1) {
            limit = 5;
        }
        if (limit > 20) {
            limit = 20;
        }
        Page<Notice> page = this.page(new Page<Notice>(1, limit),
                new LambdaQueryWrapper<Notice>().orderByDesc(Notice::getPublishTime));
        return page.getRecords();
    }

    /**
     * 发布公告，发布时间取当前时间。
     */
    @Override
    public void createNotice(NoticeSaveDTO dto) {
        Notice notice = new Notice();
        notice.setTitle(dto.getTitle().trim());
        notice.setContent(dto.getContent().trim());
        notice.setPublishTime(new Date());
        this.save(notice);
    }

    /**
     * 修改公告后刷新发布时间，让修改后的内容回到列表前面。
     */
    @Override
    public void updateNotice(NoticeSaveDTO dto) {
        if (dto.getNoticeId() == null) {
            throw new BizException("缺少公告编号");
        }
        Notice notice = this.getById(dto.getNoticeId());
        if (notice == null) {
            throw new BizException("公告不存在");
        }
        notice.setTitle(dto.getTitle().trim());
        notice.setContent(dto.getContent().trim());
        notice.setPublishTime(new Date());
        this.updateById(notice);
    }

    /**
     * 删除过期或错误公告。
     */
    @Override
    public void deleteNotice(Long noticeId) {
        Notice notice = this.getById(noticeId);
        if (notice == null) {
            throw new BizException("公告不存在");
        }
        this.removeById(noticeId);
    }
}
