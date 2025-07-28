package com.code.codeagent.controller;

import com.code.codeagent.common.BaseResponse;
import com.code.codeagent.common.ResultUtils;
import com.code.codeagent.model.dto.UserLoginRequest;
import com.code.codeagent.model.dto.UserRegisterRequest;
import com.code.codeagent.model.dto.UserUpdateMyRequest;
import com.code.codeagent.model.entity.User;
import com.code.codeagent.model.vo.LoginUserVO;
import com.code.codeagent.model.vo.UserVO;
import com.code.codeagent.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;

/**
 * 用户接口
 *
 * @author CodeAgent
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 用户 id
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册")
    public BaseResponse<Long> userRegister(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        
        long result = userService.userRegister(userAccount, userPassword, checkPassword, 
                userRegisterRequest.getUserName(), userRegisterRequest.getUserEmail());
        
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @return 登录用户信息
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录")
    public BaseResponse<LoginUserVO> userLogin(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        
        User user = userService.userLogin(userAccount, userPassword);
        
        // 构造返回结果
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        loginUserVO.setToken(StpUtil.getTokenValue());
        
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户注销
     *
     * @return 是否成功
     */
    @PostMapping("/logout")
    @Operation(summary = "用户注销", description = "用户注销")
    public BaseResponse<Boolean> userLogout() {
        boolean result = userService.userLogout();
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @return 当前登录用户信息
     */
    @GetMapping("/get/login")
    @SaCheckLogin
    @Operation(summary = "获取当前登录用户", description = "获取当前登录用户")
    public BaseResponse<LoginUserVO> getLoginUser() {
        User user = userService.getLoginUser();
        
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        loginUserVO.setToken(StpUtil.getTokenValue());
        
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id 用户id
     * @return 用户信息
     */
    @GetMapping("/get")
    @SaCheckLogin
    @Operation(summary = "根据id获取用户", description = "根据id获取用户（仅管理员）")
    public BaseResponse<User> getUserById(long id) {
        User user = userService.getById(id);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id 用户id
     * @return 用户信息
     */
    @GetMapping("/get/vo")
    @Operation(summary = "根据id获取用户信息", description = "根据id获取脱敏的用户信息")
    public BaseResponse<UserVO> getUserVOById(long id) {
        User user = userService.getById(id);
        User safetyUser = userService.getSafetyUser(user);
        
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(safetyUser, userVO);
        
        return ResultUtils.success(userVO);
    }

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest 用户更新请求
     * @return 是否成功
     */
    @PostMapping("/update/my")
    @SaCheckLogin
    @Operation(summary = "更新个人信息", description = "更新当前登录用户的个人信息")
    public BaseResponse<Boolean> updateMyUser(@Valid @RequestBody UserUpdateMyRequest userUpdateMyRequest) {
        User loginUser = userService.getLoginUser();
        boolean result = userService.updateMyUser(loginUser, userUpdateMyRequest);
        return ResultUtils.success(result);
    }
}
