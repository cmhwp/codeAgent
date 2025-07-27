package com.code.codeagent.common;

import com.code.codeagent.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用响应封装类
 * 用于统一API响应格式，提供一致的数据结构
 *
 * @param <T> 响应数据类型
 * @author CodeAgent
 * @since 1.0.0
 */
@Data
public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     * 0 - 成功，其他值表示各种错误状态
     */
    private int code;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 完整构造函数
     *
     * @param code    状态码
     * @param data    数据
     * @param message 消息
     */
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /**
     * 构造函数（无消息）
     *
     * @param code 状态码
     * @param data 数据
     */
    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    /**
     * 错误响应构造函数
     *
     * @param errorCode 错误码枚举
     */
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }

    /**
     * 默认构造函数
     */
    public BaseResponse() {
    }

    /**
     * 判断响应是否成功
     *
     * @return true-成功，false-失败
     */
    public boolean isSuccess() {
        return this.code == 0;
    }
} 