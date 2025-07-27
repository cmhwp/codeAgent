package com.code.codeagent.exception;

import lombok.Getter;

/**
 * 错误码枚举
 * 定义系统中所有的错误码和对应的错误信息
 * 
 * 错误码规范：
 * - 0: 成功
 * - 400xx: 客户端错误
 * - 401xx: 认证相关错误
 * - 403xx: 权限相关错误
 * - 404xx: 资源不存在错误
 * - 500xx: 服务器内部错误
 *
 * @author CodeAgent
 * @since 1.0.0
 */
@Getter
public enum ErrorCode {

    /**
     * 成功
     */
    SUCCESS(0, "操作成功"),

    /**
     * 客户端错误 - 4xxxx
     */
    PARAMS_ERROR(40000, "请求参数错误"),
    PARAMS_NULL_ERROR(40001, "请求参数为空"),
    PARAMS_FORMAT_ERROR(40002, "请求参数格式错误"),

    /**
     * 认证相关错误 - 401xx
     */
    NOT_LOGIN_ERROR(40100, "用户未登录"),
    LOGIN_ERROR(40101, "用户名或密码错误"),
    TOKEN_EXPIRED_ERROR(40102, "登录令牌已过期"),
    TOKEN_INVALID_ERROR(40103, "登录令牌无效"),

    /**
     * 权限相关错误 - 403xx
     */
    NO_AUTH_ERROR(40300, "用户无权限访问"),
    FORBIDDEN_ERROR(40301, "禁止访问"),
    ROLE_ERROR(40302, "用户角色权限不足"),

    /**
     * 资源不存在错误 - 404xx
     */
    NOT_FOUND_ERROR(40400, "请求的资源不存在"),
    USER_NOT_FOUND_ERROR(40401, "用户不存在"),
    DATA_NOT_FOUND_ERROR(40402, "数据不存在"),

    /**
     * 业务逻辑错误 - 409xx
     */
    CONFLICT_ERROR(40900, "资源冲突"),
    DUPLICATE_ERROR(40901, "数据重复"),
    STATUS_ERROR(40902, "状态错误"),

    /**
     * 服务器内部错误 - 500xx
     */
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),
    DATABASE_ERROR(50002, "数据库操作异常"),
    NETWORK_ERROR(50003, "网络异常"),
    THIRD_PARTY_ERROR(50004, "第三方服务异常"),

    /**
     * 业务限制错误 - 502xx
     */
    RATE_LIMIT_ERROR(50200, "请求过于频繁"),
    QUOTA_EXCEEDED_ERROR(50201, "配额已超限"),
    SERVICE_UNAVAILABLE_ERROR(50202, "服务暂不可用");

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误信息
     */
    private final String message;

    /**
     * 构造函数
     *
     * @param code    错误码
     * @param message 错误信息
     */
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据错误码获取对应的枚举
     *
     * @param code 错误码
     * @return 对应的错误码枚举，如果没找到则返回null
     */
    public static ErrorCode getByCode(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }

    /**
     * 判断是否为成功状态码
     *
     * @param code 状态码
     * @return true-成功，false-失败
     */
    public static boolean isSuccess(int code) {
        return SUCCESS.getCode() == code;
    }
} 