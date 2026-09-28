package com.office.purchase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 前端打包进后端后，刷新子路径时转发到首页，由 Vue Router 接管。
 */
@Controller
public class SpaController {

    /**
     * 只转发页面路径，不转发 /user、/goods 等接口。
     */
    @RequestMapping({
            "/",
            "/login",
            "/home",
            "/goods",
            "/apply",
            "/apply/submit",
            "/apply/mine",
            "/audit",
            "/order",
            "/user",
            "/notice",
            "/password"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
