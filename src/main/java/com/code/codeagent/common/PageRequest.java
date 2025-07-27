package com.code.codeagent.common;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 分页请求封装类
 * 用于统一处理分页查询的请求参数
 *
 * @author CodeAgent
 * @since 1.0.0
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页号，从1开始
     */
    @Min(value = 1, message = "页号不能小于1")
    private int pageNum = 1;

    /**
     * 页面大小，限制在1-100之间
     */
    @Min(value = 1, message = "页面大小不能小于1")
    @Max(value = 100, message = "页面大小不能超过100")
    private int pageSize = 10;

    /**
     * 排序字段
     * 应该是数据库表中的真实字段名
     */
    private String sortField;

    /**
     * 排序顺序
     * 只允许 "ascend" 或 "descend"
     */
    @Pattern(regexp = "^(ascend|descend)$", message = "排序顺序只能是ascend或descend")
    private String sortOrder = "descend";

    /**
     * 默认构造函数
     */
    public PageRequest() {
    }

    /**
     * 构造函数
     *
     * @param pageNum  页号
     * @param pageSize 页面大小
     */
    public PageRequest(int pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    /**
     * 构造函数
     *
     * @param pageNum   页号
     * @param pageSize  页面大小
     * @param sortField 排序字段
     * @param sortOrder 排序顺序
     */
    public PageRequest(int pageNum, int pageSize, String sortField, String sortOrder) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    /**
     * 计算偏移量（用于SQL查询）
     *
     * @return 偏移量
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }

    /**
     * 检查是否为升序排序
     *
     * @return true-升序，false-降序
     */
    public boolean isAscending() {
        return "ascend".equals(sortOrder);
    }
} 