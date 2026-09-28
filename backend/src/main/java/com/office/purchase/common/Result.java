package com.office.purchase.common;

import lombok.Data;

/**
 * 统一接口返回结构，与论文中的 code / msg / data 约定一致。
 */
@Data
public class Result<T> {

    /** 200 成功，401 未登录，403 无权限，500 业务失败 */
    private int code;

    /** 给页面直接展示的说明 */
    private String msg;

    /** 业务数据，失败时为空 */
    private T data;

    /**
     * 返回成功结果。
     */
    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<T>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 返回带自定义文案的成功结果。
     */
    public static <T> Result<T> ok(String msg, T data) {
        Result<T> result = ok(data);
        result.setMsg(msg);
        return result;
    }

    /**
     * 返回默认的业务失败结果。
     */
    public static <T> Result<T> fail(String msg) {
        return fail(500, msg);
    }

    /**
     * 返回指定状态码的失败结果。
     */
    public static <T> Result<T> fail(int code, String msg) {
        Result<T> result = new Result<T>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
