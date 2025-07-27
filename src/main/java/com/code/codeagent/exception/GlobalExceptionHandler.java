package com.code.codeagent.exception;

import com.code.codeagent.common.BaseResponse;
import com.code.codeagent.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理系统中的各种异常，提供一致的错误响应格式
 * 
 * 处理的异常类型：
 * 1. 业务异常 - BusinessException
 * 2. 参数校验异常 - MethodArgumentNotValidException, BindException等
 * 3. 系统异常 - RuntimeException
 * 4. 其他常见Web异常
 *
 * @author CodeAgent
 * @since 1.0.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e       业务异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 - URI: {}, 错误码: {}, 错误信息: {}", 
                request.getRequestURI(), e.getCode(), e.getMessage());
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid注解校验失败）
     *
     * @param e       参数校验异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<Void> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("参数校验失败 - URI: {}", request.getRequestURI());
        
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, errorMessage);
    }

    /**
     * 处理参数绑定异常
     *
     * @param e       参数绑定异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<Void> handleBindException(BindException e, HttpServletRequest request) {
        log.warn("参数绑定失败 - URI: {}", request.getRequestURI());
        
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, errorMessage);
    }

    /**
     * 处理约束校验异常
     *
     * @param e       约束校验异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.warn("约束校验失败 - URI: {}", request.getRequestURI());
        
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String errorMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, errorMessage);
    }

    /**
     * 处理缺少请求参数异常
     *
     * @param e       缺少请求参数异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<Void> handleMissingParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("缺少请求参数 - URI: {}, 参数名: {}", request.getRequestURI(), e.getParameterName());
        return ResultUtils.error(ErrorCode.PARAMS_NULL_ERROR, "缺少必需参数: " + e.getParameterName());
    }

    /**
     * 处理参数类型不匹配异常
     *
     * @param e       参数类型不匹配异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<Void> handleTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("参数类型不匹配 - URI: {}, 参数名: {}", request.getRequestURI(), e.getName());
        return ResultUtils.error(ErrorCode.PARAMS_FORMAT_ERROR, "参数格式错误: " + e.getName());
    }

    /**
     * 处理请求体不可读异常
     *
     * @param e       请求体不可读异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("请求体解析失败 - URI: {}", request.getRequestURI());
        return ResultUtils.error(ErrorCode.PARAMS_FORMAT_ERROR, "请求体格式错误");
    }

    /**
     * 处理请求方法不支持异常
     *
     * @param e       请求方法不支持异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public BaseResponse<Void> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持 - URI: {}, 方法: {}", request.getRequestURI(), request.getMethod());
        return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "请求方法不支持: " + e.getMethod());
    }

    /**
     * 处理404异常
     *
     * @param e       404异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseResponse<Void> handleNotFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("请求路径不存在 - URI: {}", request.getRequestURI());
        return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "请求路径不存在");
    }

    /**
     * 处理静态资源不存在异常（如favicon.ico）
     *
     * @param e       静态资源不存在异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseResponse<Void> handleNoResourceFoundException(org.springframework.web.servlet.resource.NoResourceFoundException e, HttpServletRequest request) {
        // 对于favicon.ico等静态资源请求，只记录debug级别日志，避免污染日志
        if (request.getRequestURI().contains("favicon.ico")) {
            log.debug("静态资源请求 - URI: {}", request.getRequestURI());
        } else {
            log.warn("静态资源不存在 - URI: {}", request.getRequestURI());
        }
        return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "资源不存在");
    }

    /**
     * 处理运行时异常
     *
     * @param e       运行时异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseResponse<Void> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("运行时异常 - URI: {}, 异常信息: {}", request.getRequestURI(), e.getMessage(), e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统内部错误");
    }

    /**
     * 处理其他异常
     *
     * @param e       其他异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseResponse<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("未知异常 - URI: {}, 异常信息: {}", request.getRequestURI(), e.getMessage(), e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统异常，请联系管理员");
    }
} 