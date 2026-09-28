package com.office.purchase.common;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 分页结果。前端表格只依赖 total 和 records。
 */
@Data
public class PageResult<T> {

    private long total;
    private long current;
    private long size;
    private List<T> records;

    /**
     * 构造一页数据。
     */
    public static <T> PageResult<T> of(long total, long current, long size, List<T> records) {
        PageResult<T> page = new PageResult<T>();
        page.setTotal(total);
        page.setCurrent(current);
        page.setSize(size);
        page.setRecords(records == null ? Collections.<T>emptyList() : records);
        return page;
    }

    /**
     * 构造空页，用于员工没有订单等场景。
     */
    public static <T> PageResult<T> empty(long current, long size) {
        return of(0, current, size, Collections.<T>emptyList());
    }
}
