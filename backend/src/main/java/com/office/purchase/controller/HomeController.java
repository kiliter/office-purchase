package com.office.purchase.controller;

import com.office.purchase.common.Result;
import com.office.purchase.common.UserContext;
import com.office.purchase.service.HomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 登录后的工作台。
 */
@RestController
@RequestMapping("/home")
public class HomeController {

    @Resource
    private HomeService homeService;

    /**
     * 返回当前角色的待办数量和最新公告。
     */
    @GetMapping("/summary")
    public Result<?> summary() {
        return Result.ok(homeService.summary(UserContext.get()));
    }
}
