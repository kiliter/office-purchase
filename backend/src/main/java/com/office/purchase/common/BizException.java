package com.office.purchase.common;

/**
 * 可预期的业务错误，例如状态不允许审批、商品不存在。
 * 全局异常处理会把它转换成前端可阅读的提示。
 */
public class BizException extends RuntimeException {

    /**
     * @param message 直接展示给用户的原因
     */
    public BizException(String message) {
        super(message);
    }
}
