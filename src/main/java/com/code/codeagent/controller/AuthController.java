package com.code.codeagent.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.code.codeagent.common.BaseResponse;
import com.code.codeagent.common.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证状态控制器
 * 提供Sa-Token相关的认证状态查询接口
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "authStatus", description = "Sa-Token认证状态相关接口")
@Slf4j
public class AuthController {

    /**
     * 查询登录状态
     */
    @GetMapping("/isLogin")
    @Operation(summary = "查询登录状态", description = "查询当前用户是否已登录")
    public BaseResponse<Map<String, Object>> isLogin() {
        Map<String, Object> result = new HashMap<>();
        result.put("isLogin", StpUtil.isLogin());
        result.put("loginId", StpUtil.getLoginIdDefaultNull());
        if (StpUtil.isLogin()) {
            result.put("tokenValue", StpUtil.getTokenValue());
            result.put("loginDevice", StpUtil.getLoginDevice());
        }
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/userInfo")
    @SaCheckLogin
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    public BaseResponse<Map<String, Object>> getUserInfo() {
        Object loginId = StpUtil.getLoginId();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("loginId", loginId);
        userInfo.put("tokenValue", StpUtil.getTokenValue());
        userInfo.put("loginDevice", StpUtil.getLoginDevice());
        userInfo.put("roleList", StpUtil.getRoleList());
        userInfo.put("permissionList", StpUtil.getPermissionList());
        
        return ResultUtils.success(userInfo);
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    @Operation(summary = "用户注销", description = "退出登录")
    public BaseResponse<String> logout() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        StpUtil.logout();
        log.info("用户 {} 注销成功", loginId);
        return ResultUtils.success("注销成功");
    }

    /**
     * 踢人下线
     */
    @PostMapping("/kickout")
    @SaCheckLogin
    @Operation(summary = "踢人下线", description = "将指定用户踢下线")
    public BaseResponse<String> kickout(@RequestParam String loginId) {
        // 先校验一下，要踢的人是否在线
        if (StpUtil.isLogin(loginId)) {
            // 踢人下线
            StpUtil.kickout(loginId);
            log.info("用户 {} 被踢下线", loginId);
            return ResultUtils.success("踢人成功");
        } else {
            return ResultUtils.error(40004, "该用户不在线");
        }
    }

    /**
     * Token 相关信息
     */
    @GetMapping("/tokenInfo")
    @SaCheckLogin
    @Operation(summary = "Token信息", description = "获取当前Token的详细信息")
    public BaseResponse<Map<String, Object>> tokenInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("tokenName", StpUtil.getTokenName());
        result.put("tokenValue", StpUtil.getTokenValue());
        result.put("loginId", StpUtil.getLoginId());
        result.put("loginDevice", StpUtil.getLoginDevice());
        result.put("tokenTimeout", StpUtil.getTokenTimeout());
        result.put("sessionTimeout", StpUtil.getSessionTimeout());
        result.put("tokenSessionTimeout", StpUtil.getTokenSessionTimeout());
        result.put("tokenActiveTimeout", StpUtil.getTokenActiveTimeout());
        result.put("loginType", StpUtil.getLoginType());
        
        return ResultUtils.success(result);
    }
} 