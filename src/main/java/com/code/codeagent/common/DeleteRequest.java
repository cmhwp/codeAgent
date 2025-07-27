package com.code.codeagent.common;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;

/**
 * 删除请求包装类
 * 用于统一处理删除操作的请求参数
 *
 * @author CodeAgent
 * @since 1.0.0
 */
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 要删除的记录ID
     * 必须是正整数
     */
    @NotNull(message = "删除ID不能为空")
    @Positive(message = "删除ID必须是正整数")
    private Long id;

    /**
     * 默认构造函数
     */
    public DeleteRequest() {
    }

    /**
     * 构造函数
     *
     * @param id 要删除的记录ID
     */
    public DeleteRequest(Long id) {
        this.id = id;
    }
} 