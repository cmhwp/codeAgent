package com.code.codeagent.controller;

import com.code.codeagent.common.BaseResponse;
import com.code.codeagent.common.DeleteRequest;
import com.code.codeagent.common.PageRequest;
import com.code.codeagent.common.ResultUtils;
import com.code.codeagent.exception.BusinessException;
import com.code.codeagent.exception.ErrorCode;
import com.code.codeagent.exception.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;

/**
 * 示例控制器
 * 展示如何使用common和exception包中的通用代码
 *
 * @author CodeAgent
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/example")
@Tag(name = "示例接口", description = "展示如何使用通用响应和异常处理")
@Slf4j
public class ExampleController {

    /**
     * 成功响应示例（返回数据）
     *
     * @return 包含数据的成功响应
     */
    @GetMapping("/success")
    @Operation(summary = "成功响应示例", description = "展示如何返回成功响应")
    public BaseResponse<List<String>> successExample() {
        List<String> data = Arrays.asList("数据1", "数据2", "数据3");
        return ResultUtils.success(data);
    }

    /**
     * 成功响应示例（无数据）
     *
     * @return 无数据的成功响应
     */
    @PostMapping("/success-no-data")
    @Operation(summary = "成功响应示例（无数据）", description = "展示如何返回无数据的成功响应")
    public BaseResponse<Void> successNoDataExample() {
        // 模拟业务操作
        log.info("执行了某项操作");
        return ResultUtils.success();
    }

    /**
     * 参数校验示例
     *
     * @param request 分页请求参数
     * @return 成功响应
     */
    @PostMapping("/validation")
    @Operation(summary = "参数校验示例", description = "展示参数校验功能")
    public BaseResponse<String> validationExample(@Valid @RequestBody PageRequest request) {
        String result = String.format("页号: %d, 页面大小: %d, 排序字段: %s, 排序顺序: %s",
                request.getPageNum(), request.getPageSize(), 
                request.getSortField(), request.getSortOrder());
        return ResultUtils.success(result);
    }

    /**
     * 删除操作示例
     *
     * @param request 删除请求参数
     * @return 成功响应
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除操作示例", description = "展示删除操作参数校验")
    public BaseResponse<Void> deleteExample(@Valid @RequestBody DeleteRequest request) {
        // 使用ThrowUtils进行业务逻辑检查
        ThrowUtils.throwIfNotPositive(request.getId(), ErrorCode.PARAMS_ERROR, "ID必须大于0");
        
        // 模拟检查资源是否存在
        if (request.getId() > 1000) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "资源不存在");
        }
        
        // 模拟删除操作
        log.info("删除ID为{}的资源", request.getId());
        return ResultUtils.success();
    }

    /**
     * 业务异常示例
     *
     * @param type 异常类型
     * @return 不会正常返回，会抛出异常
     */
    @GetMapping("/error/{type}")
    @Operation(summary = "业务异常示例", description = "展示不同类型的业务异常")
    public BaseResponse<Void> errorExample(@PathVariable String type) {
        switch (type) {
            case "business":
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "模拟业务操作失败");
            case "validation":
                ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "模拟参数验证失败");
                break;
            case "notfound":
                ThrowUtils.throwIf(true, ErrorCode.NOT_FOUND_ERROR);
                break;
            case "auth":
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            case "system":
                throw new RuntimeException("模拟系统异常");
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "未知的异常类型: " + type);
        }
        return ResultUtils.success();
    }

    /**
     * ThrowUtils工具类使用示例
     *
     * @param value 测试值
     * @return 成功响应
     */
    @GetMapping("/throw-utils")
    @Operation(summary = "ThrowUtils工具类示例", description = "展示ThrowUtils工具类的使用")
    public BaseResponse<String> throwUtilsExample(@RequestParam(required = false) String value) {
        // 检查参数不为空
        ThrowUtils.throwIfBlank(value, ErrorCode.PARAMS_NULL_ERROR, "参数value不能为空");
        
        // 检查参数长度
        ThrowUtils.throwIf(value.length() > 10, ErrorCode.PARAMS_ERROR, "参数长度不能超过10");
        
        // 检查参数值
        ThrowUtils.throwIfNotEquals(value, "test", ErrorCode.PARAMS_ERROR, "参数值必须是'test'");
        
        return ResultUtils.success("参数校验通过: " + value);
    }

    /**
     * 分页查询示例
     *
     * @param pageRequest 分页参数
     * @return 分页数据
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询示例", description = "展示分页查询的使用")
    public BaseResponse<String> pageExample(@Valid @RequestBody PageRequest pageRequest) {
        // 使用分页参数
        int offset = pageRequest.getOffset();
        boolean isAscending = pageRequest.isAscending();
        
        String result = String.format("查询偏移量: %d, 是否升序: %s", offset, isAscending);
        log.info("分页查询 - {}", result);
        
        return ResultUtils.success(result);
    }
} 