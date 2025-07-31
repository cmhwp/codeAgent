package com.code.codeagent.common;

import com.code.codeagent.exception.ErrorCode;

/**
 * 快速构造响应结果的工具类
 * 提供便捷的成功和失败响应构造方法
 *
 * @author CodeAgent
 * @since 1.0.0
 */
public class ResultUtils {

    /**
     * 私有构造函数，防止实例化
     */
    private ResultUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 成功响应（无数据）
     *
     * @return 成功响应
     */
    public static BaseResponse<Void> success() {
        return success(null);
    }

    /**
     * 成功响应（带数据和自定义消息）
     *
     * @param data    响应数据
     * @param message 自定义消息
     * @param <T>     数据类型
     * @return 成功响应
     */
    public static <T> BaseResponse<T> success(T data, String message) {
        return new BaseResponse<>(0, data, message);
    }

    /**
     * 失败响应（使用错误码枚举）
     *
     * @param errorCode 错误码枚举
     * @return 失败响应
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode.getCode(), null, errorCode.getMessage());
    }

    /**
     * 失败响应（自定义错误码和消息）
     *
     * @param code    错误码
     * @param message 错误消息
     * @return 失败响应
     */
    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * 失败响应（使用错误码枚举并自定义消息）
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息
     * @return 失败响应
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }

    /**
     * 失败响应（使用错误码枚举、自定义消息和数据）
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息
     * @param data      响应数据
     * @param <T>       数据类型
     * @return 失败响应
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message, T data) {
        return new BaseResponse<>(errorCode.getCode(), data, message);
    }

    /**
     * 系统错误响应
     *
     * @return 系统错误响应
     */
    public static <T> BaseResponse<T> systemError() {
        return error(ErrorCode.SYSTEM_ERROR);
    }

    /**
     * 参数错误响应
     *
     * @return 参数错误响应
     */
    public static <T> BaseResponse<T> paramError() {
        return error(ErrorCode.PARAMS_ERROR);
    }

    /**
     * 未登录错误响应
     *
     * @return 未登录错误响应
     */
    public static <T> BaseResponse<T> notLoginError() {
        return error(ErrorCode.NOT_LOGIN_ERROR);
    }

    /**
     * 无权限错误响应
     *
     * @return 无权限错误响应
     */
    public static <T> BaseResponse<T> noAuthError() {
        return error(ErrorCode.NO_AUTH_ERROR);
    }

    /**
     * 资源不存在错误响应
     *
     * @return 资源不存在错误响应
     */
    public static <T> BaseResponse<T> notFoundError() {
        return error(ErrorCode.NOT_FOUND_ERROR);
    }
} 