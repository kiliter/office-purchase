package com.office.purchase.controller;

import com.office.purchase.annotation.RequireRole;
import com.office.purchase.common.Result;
import com.office.purchase.common.RoleConst;
import com.office.purchase.dto.GoodsSaveDTO;
import com.office.purchase.service.GoodsService;
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
 * 办公用品商品接口。查询对所有登录用户开放，维护只对管理员开放。
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private GoodsService goodsService;

    /**
     * 分页查询商品。申请页面会一次取较多记录用于勾选。
     */
    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1") long current,
                          @RequestParam(defaultValue = "10") long size,
                          @RequestParam(required = false) String goodsName,
                          @RequestParam(required = false) String goodsType) {
        return Result.ok(goodsService.pageGoods(current, size, goodsName, goodsType));
    }

    /**
     * 管理员新增商品。
     */
    @PostMapping
    @RequireRole(RoleConst.ADMIN)
    public Result<Void> create(@Valid @RequestBody GoodsSaveDTO dto) {
        goodsService.createGoods(dto);
        return Result.ok("商品已保存", null);
    }

    /**
     * 管理员修改商品。
     */
    @PutMapping
    @RequireRole(RoleConst.ADMIN)
    public Result<Void> update(@Valid @RequestBody GoodsSaveDTO dto) {
        goodsService.updateGoods(dto);
        return Result.ok("商品已保存", null);
    }

    /**
     * 管理员删除未被引用的商品。
     */
    @DeleteMapping("/{goodsId}")
    @RequireRole(RoleConst.ADMIN)
    public Result<Void> delete(@PathVariable Long goodsId) {
        goodsService.deleteGoods(goodsId);
        return Result.ok("商品已删除", null);
    }
}
