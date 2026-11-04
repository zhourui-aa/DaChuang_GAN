package com.integrity.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 统一分页结果（与前端表格分页对齐）。
 *
 * <p>字段名与 Element Plus / vue-next-admin 的分页习惯保持一致：
 * records（当前页数据）、total（总数）、pageNum、pageSize。
 *
 * @param <T> 行数据类型
 */
@Data
public class PageResult<T> implements Serializable {

    /** 当前页数据 */
    private List<T> records = Collections.emptyList();

    /** 总记录数 */
    private long total;

    /** 当前页码（从 1 开始） */
    private long pageNum;

    /** 每页条数 */
    private long pageSize;

    /** 直接由 MyBatis-Plus 的 IPage 转换。 */
    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(page.getRecords());
        r.setTotal(page.getTotal());
        r.setPageNum(page.getCurrent());
        r.setPageSize(page.getSize());
        return r;
    }

    /** 由 IPage 转换并做实体 → VO 映射。 */
    public static <E, T> PageResult<T> of(IPage<E> page, Function<E, T> mapper) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(page.getRecords().stream().map(mapper).collect(Collectors.toList()));
        r.setTotal(page.getTotal());
        r.setPageNum(page.getCurrent());
        r.setPageSize(page.getSize());
        return r;
    }
}
