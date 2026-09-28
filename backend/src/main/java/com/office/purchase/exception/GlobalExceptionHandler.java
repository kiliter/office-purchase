package com.office.purchase.exception;

import com.office.purchase.common.BizException;
import com.office.purchase.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 把参数错误和业务错误转换成统一 JSON，避免把堆栈直接返回给页面。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 表单校验失败时，返回第一条中文提示。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String msg = fieldError == null ? "提交的内容不正确" : fieldError.getDefaultMessage();
        return Result.fail(msg);
    }

    /**
     * 业务规则不满足，例如重复审批、驳回后才能修改。
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException ex) {
        return Result.fail(ex.getMessage());
    }

    /**
     * 唯一约束或外键约束失败时给出可理解的说明。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleData(DataIntegrityViolationException ex) {
        log.warn("数据约束冲突", ex);
        return Result.fail("保存失败，请检查是否重复，或仍被其他记录引用");
    }

    /**
     * 未预料的错误只记日志，页面看到统一提示。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception ex) {
        log.error("未处理异常", ex);
        return Result.fail("系统繁忙，请稍后再试");
    }
}
