package com.office.purchase.controller;

import com.office.purchase.annotation.RequireRole;
import com.office.purchase.common.Result;
import com.office.purchase.common.RoleConst;
import com.office.purchase.dto.NoticeSaveDTO;
import com.office.purchase.service.NoticeService;
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
 * 系统公告接口。查看对所有登录用户开放，维护只对管理员开放。
 */
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    /**
     * 分页查询公告。
     */
    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1") long current,
                          @RequestParam(defaultValue = "10") long size,
                          @RequestParam(required = false) String title) {
        return Result.ok(noticeService.pageNotices(current, size, title));
    }

    /**
     * 工作台使用的最新公告。
     */
    @GetMapping("/latest")
    public Result<?> latest(@RequestParam(defaultValue = "5") int limit) {
        return Result.ok(noticeService.latest(limit));
    }

    /**
     * 管理员发布公告。
     */
    @PostMapping
    @RequireRole(RoleConst.ADMIN)
    public Result<Void> create(@Valid @RequestBody NoticeSaveDTO dto) {
        noticeService.createNotice(dto);
        return Result.ok("公告已发布", null);
    }

    /**
     * 管理员修改公告。
     */
    @PutMapping
    @RequireRole(RoleConst.ADMIN)
    public Result<Void> update(@Valid @RequestBody NoticeSaveDTO dto) {
        noticeService.updateNotice(dto);
        return Result.ok("公告已保存", null);
    }

    /**
     * 管理员删除公告。
     */
    @DeleteMapping("/{noticeId}")
    @RequireRole(RoleConst.ADMIN)
    public Result<Void> delete(@PathVariable Long noticeId) {
        noticeService.deleteNotice(noticeId);
        return Result.ok("公告已删除", null);
    }
}
