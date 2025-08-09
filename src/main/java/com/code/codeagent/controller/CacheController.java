package com.code.codeagent.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.code.codeagent.common.BaseResponse;
import com.code.codeagent.common.ResultUtils;
import com.code.codeagent.config.AiCodeGeneratorServiceFactory;
import com.code.codeagent.constant.UserConstant;
import com.code.codeagent.exception.BusinessException;
import com.code.codeagent.exception.ErrorCode;

import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 缓存管理控制器
 * 用于管理AI服务实例缓存
 *
 * @author CodeAgent
 */
@RestController
@RequestMapping("/cache")
@Slf4j
@Tag(name = "CacheController", description = "缓存管理控制器")
public class CacheController {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 获取缓存统计信息
     */
    @GetMapping("/stats")
    @SaCheckLogin
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    @Operation(summary = "获取缓存统计", description = "获取AI服务缓存的统计信息（管理员）")
    public BaseResponse<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = aiCodeGeneratorServiceFactory.getCacheStats();
        return ResultUtils.success(stats);
    }

    /**
     * 清除指定应用的缓存
     */
    @DeleteMapping("/evict/{appId}")
    @SaCheckLogin
    @Operation(summary = "清除应用缓存", description = "清除指定应用的AI服务缓存")
    public BaseResponse<Boolean> evictCache(
            @Parameter(description = "应用ID") @PathVariable Long appId) {
        
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID不能为空或无效");
        }
        
        try {
            aiCodeGeneratorServiceFactory.evictCache(appId);
            log.info("用户清除了 appId: {} 的缓存", appId);
            return ResultUtils.success(true);
        } catch (Exception e) {
            log.error("清除缓存失败，appId: {}, 错误: {}", appId, e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "清除缓存失败");
        }
    }

    /**
     * 清除所有缓存（管理员功能）
     */
    @DeleteMapping("/evict/all")
    @SaCheckLogin
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    @Operation(summary = "清除所有缓存", description = "清除所有AI服务缓存（管理员功能）")
    public BaseResponse<Boolean> evictAllCache() {
        try {
            aiCodeGeneratorServiceFactory.evictAllCaches();
            log.info("管理员清除了所有AI服务缓存");
            return ResultUtils.success(true);
        } catch (Exception e) {
            log.error("清除所有缓存失败，错误: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "清除所有缓存失败");
        }
    }

    /**
     * 预热缓存
     */
    @PostMapping("/warmup/{appId}")
    @SaCheckLogin
    @Operation(summary = "预热缓存", description = "为指定应用预热AI服务缓存")
    public BaseResponse<Boolean> warmupCache(
            @Parameter(description = "应用ID") @PathVariable Long appId) {
        
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID不能为空或无效");
        }
        
        try {
            // 预热缓存，触发AI服务实例的创建
            aiCodeGeneratorServiceFactory.warmupCache(appId);
            log.info("已为 appId: {} 预热缓存", appId);
            return ResultUtils.success(true);
        } catch (Exception e) {
            log.error("预热缓存失败，appId: {}, 错误: {}", appId, e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "预热缓存失败");
        }
    }
}