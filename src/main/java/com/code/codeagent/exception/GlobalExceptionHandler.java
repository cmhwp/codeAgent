package com.code.codeagent.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SaTokenException;
import com.code.codeagent.common.BaseResponse;
import com.code.codeagent.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * Sa-Token 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> handlerNotLoginException(NotLoginException nle) {
        log.error("NotLoginException", nle);
        
        // 判断场景值，定制化异常信息
        String message = "";
        if(nle.getType().equals(NotLoginException.NOT_TOKEN)) {
            message = "未提供Token";
        }
        else if(nle.getType().equals(NotLoginException.INVALID_TOKEN)) {
            message = "Token无效";
        }
        else if(nle.getType().equals(NotLoginException.TOKEN_TIMEOUT)) {
            message = "Token已过期";
        }
        else if(nle.getType().equals(NotLoginException.BE_REPLACED)) {
            message = "Token已被顶下线";
        }
        else if(nle.getType().equals(NotLoginException.KICK_OUT)) {
            message = "Token已被踢下线";
        }
        else if(nle.getType().equals(NotLoginException.TOKEN_FREEZE)) {
            message = "Token已被冻结";
        }
        else {
            message = "当前会话未登录";
        }
        
        return ResultUtils.error(40101, message);
    }

    /**
     * Sa-Token 缺少权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public BaseResponse<?> handlerNotPermissionException(NotPermissionException e) {
        log.error("NotPermissionException", e);
        return ResultUtils.error(40301, "无此权限：" + e.getPermission());
    }

    /**
     * Sa-Token 缺少角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    public BaseResponse<?> handlerNotRoleException(NotRoleException e) {
        log.error("NotRoleException", e);
        return ResultUtils.error(40302, "无此角色：" + e.getRole());
    }

    /**
     * Sa-Token 其他异常
     */
    @ExceptionHandler(SaTokenException.class)
    public BaseResponse<?> handlerSaTokenException(SaTokenException e, HttpServletRequest request) {
        log.error("SaTokenException", e);
        
        // 检查是否是SSE流式请求，如果是则不处理，避免Content-Type冲突
        String contentType = request.getHeader("Accept");
        String requestURI = request.getRequestURI();
        
        if (contentType != null && contentType.contains("text/event-stream") ||
            requestURI != null && requestURI.contains("/stream/")) {
            // 对于SSE请求，让它自己处理异常，不返回JSON响应
            log.warn("SaToken异常发生在SSE流式请求中，已忽略全局异常处理: {}", requestURI);
            throw e; // 重新抛出让流式处理器自己处理
        }
        
        return ResultUtils.error(40100, "认证失败：" + e.getMessage());
    }

    /**
     * 静态资源未找到异常（特殊处理favicon.ico等）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public BaseResponse<?> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        
        // 对于favicon.ico等常见静态资源请求，不记录错误日志，只记录debug级别
        if (requestURI != null && (requestURI.endsWith("favicon.ico") || 
                                  requestURI.endsWith("robots.txt") ||
                                  requestURI.endsWith("apple-touch-icon.png"))) {
            log.debug("静态资源未找到（已忽略）: {}", requestURI);
            return ResultUtils.error(40404, "资源未找到");
        }
        
        // 其他资源未找到仍然记录错误日志
        log.warn("静态资源未找到: {}", requestURI);
        return ResultUtils.error(40404, "资源未找到：" + e.getResourcePath());
    }

    /**
     * 参数校验异常 - @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException", e);
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResultUtils.error(40000, "参数校验失败：" + message);
    }

    /**
     * 参数校验异常 - @Validated
     */
    @ExceptionHandler(BindException.class)
    public BaseResponse<?> handleBindException(BindException e) {
        log.error("BindException", e);
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResultUtils.error(40000, "参数校验失败：" + message);
    }

    /**
     * 参数校验异常 - @Validated (单个参数)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public BaseResponse<?> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("ConstraintViolationException", e);
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return ResultUtils.error(40000, "参数校验失败：" + message);
    }

    /**
     * 运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误：" + e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public BaseResponse<?> exceptionHandler(Exception e) {
        log.error("Exception", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统异常：" + e.getMessage());
    }
} 