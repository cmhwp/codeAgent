package com.code.codeagent.exception;

import java.util.Collection;
import java.util.Objects;

/**
 * 抛异常工具类
 * 提供便捷的条件判断和异常抛出方法
 * 
 * 使用该工具类可以简化业务代码中的异常处理逻辑，
 * 使代码更加简洁和易读
 *
 * @author CodeAgent
 * @since 1.0.0
 */
public class ThrowUtils {

    /**
     * 私有构造函数，防止实例化
     */
    private ThrowUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 条件成立则抛出异常
     *
     * @param condition        判断条件
     * @param runtimeException 要抛出的运行时异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 判断条件
     * @param errorCode 错误码枚举
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 判断条件
     * @param errorCode 错误码枚举
     * @param message   自定义错误信息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }

    /**
     * 对象为空则抛异常
     *
     * @param object    检查的对象
     * @param errorCode 错误码枚举
     */
    public static void throwIfNull(Object object, ErrorCode errorCode) {
        throwIf(Objects.isNull(object), errorCode);
    }

    /**
     * 对象为空则抛异常
     *
     * @param object    检查的对象
     * @param errorCode 错误码枚举
     * @param message   自定义错误信息
     */
    public static void throwIfNull(Object object, ErrorCode errorCode, String message) {
        throwIf(Objects.isNull(object), errorCode, message);
    }

    /**
     * 字符串为空或空白则抛异常
     *
     * @param str       检查的字符串
     * @param errorCode 错误码枚举
     */
    public static void throwIfBlank(String str, ErrorCode errorCode) {
        throwIf(Objects.isNull(str) || str.trim().isEmpty(), errorCode);
    }

    /**
     * 字符串为空或空白则抛异常
     *
     * @param str       检查的字符串
     * @param errorCode 错误码枚举
     * @param message   自定义错误信息
     */
    public static void throwIfBlank(String str, ErrorCode errorCode, String message) {
        throwIf(Objects.isNull(str) || str.trim().isEmpty(), errorCode, message);
    }

    /**
     * 集合为空或空白则抛异常
     *
     * @param collection 检查的集合
     * @param errorCode  错误码枚举
     */
    public static void throwIfEmpty(Collection<?> collection, ErrorCode errorCode) {
        throwIf(Objects.isNull(collection) || collection.isEmpty(), errorCode);
    }

    /**
     * 集合为空或空白则抛异常
     *
     * @param collection 检查的集合
     * @param errorCode  错误码枚举
     * @param message    自定义错误信息
     */
    public static void throwIfEmpty(Collection<?> collection, ErrorCode errorCode, String message) {
        throwIf(Objects.isNull(collection) || collection.isEmpty(), errorCode, message);
    }

    /**
     * 数字小于等于0则抛异常
     *
     * @param number    检查的数字
     * @param errorCode 错误码枚举
     */
    public static void throwIfNotPositive(Number number, ErrorCode errorCode) {
        throwIf(Objects.isNull(number) || number.longValue() <= 0, errorCode);
    }

    /**
     * 数字小于等于0则抛异常
     *
     * @param number    检查的数字
     * @param errorCode 错误码枚举
     * @param message   自定义错误信息
     */
    public static void throwIfNotPositive(Number number, ErrorCode errorCode, String message) {
        throwIf(Objects.isNull(number) || number.longValue() <= 0, errorCode, message);
    }

    /**
     * 两个对象不相等则抛异常
     *
     * @param obj1      对象1
     * @param obj2      对象2
     * @param errorCode 错误码枚举
     */
    public static void throwIfNotEquals(Object obj1, Object obj2, ErrorCode errorCode) {
        throwIf(!Objects.equals(obj1, obj2), errorCode);
    }

    /**
     * 两个对象不相等则抛异常
     *
     * @param obj1      对象1
     * @param obj2      对象2
     * @param errorCode 错误码枚举
     * @param message   自定义错误信息
     */
    public static void throwIfNotEquals(Object obj1, Object obj2, ErrorCode errorCode, String message) {
        throwIf(!Objects.equals(obj1, obj2), errorCode, message);
    }
} 