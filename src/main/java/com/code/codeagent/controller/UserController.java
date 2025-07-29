package com.code.codeagent.controller;

import com.code.codeagent.common.BaseResponse;
import com.code.codeagent.common.ResultUtils;
import com.code.codeagent.model.dto.*;
import com.code.codeagent.constant.PermissionConstant;
import com.code.codeagent.model.entity.User;
import com.code.codeagent.model.vo.LoginUserVO;
import com.code.codeagent.model.vo.UserVO;
import com.code.codeagent.service.MailService;
import com.code.codeagent.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户接口
 *
 * @author CodeAgent
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "userManagement", description = "用户相关接口")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private MailService mailService;

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
    @SaCheckRole("admin")
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

    /**
     * 发送验证码
     *
     * @param sendCodeRequest 发送验证码请求
     * @return 是否成功
     */
    @PostMapping("/send-code")
    @Operation(summary = "发送验证码", description = "发送邮箱验证码")
    public BaseResponse<Boolean> sendCode(@Valid @RequestBody SendCodeRequest sendCodeRequest) {
        boolean result = mailService.sendVerificationCode(sendCodeRequest.getEmail(), sendCodeRequest.getPurpose());
        return ResultUtils.success(result);
    }

    /**
     * 绑定邮箱
     *
     * @param bindEmailRequest 绑定邮箱请求
     * @return 是否成功
     */
    @PostMapping("/bind-email")
    @SaCheckLogin
    @Operation(summary = "绑定邮箱", description = "绑定邮箱到当前用户")
    public BaseResponse<Boolean> bindEmail(@Valid @RequestBody BindEmailRequest bindEmailRequest) {
        User loginUser = userService.getLoginUser();
        boolean result = userService.bindEmail(loginUser, bindEmailRequest.getEmail(), bindEmailRequest.getCode());
        return ResultUtils.success(result);
    }

    /**
     * 修改密码
     *
     * @param changePasswordRequest 修改密码请求
     * @return 是否成功
     */
    @PostMapping("/change-password")
    @SaCheckLogin
    @Operation(summary = "修改密码", description = "修改当前用户密码")
    public BaseResponse<Boolean> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        // 验证两次密码输入是否一致
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
            return ResultUtils.error(40000, "两次输入的新密码不一致");
        }

        User loginUser = userService.getLoginUser();
        boolean result = userService.changePassword(loginUser, 
                changePasswordRequest.getOldPassword(), 
                changePasswordRequest.getNewPassword());
        return ResultUtils.success(result);
    }

    /**
     * 重置密码
     *
     * @param resetPasswordRequest 重置密码请求
     * @return 是否成功
     */
    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "通过邮箱验证码重置密码")
    public BaseResponse<Boolean> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        // 验证两次密码输入是否一致
        if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmPassword())) {
            return ResultUtils.error(40000, "两次输入的新密码不一致");
        }

                 boolean result = userService.resetPassword(
                 resetPasswordRequest.getEmail(),
                 resetPasswordRequest.getCode(),
                 resetPasswordRequest.getNewPassword());
         return ResultUtils.success(result);
     }

     /**
      * 更新用户角色（仅管理员）
      *
      * @param updateUserRoleRequest 更新用户角色请求
      * @return 是否成功
      */
     @PostMapping("/update-role")
     @SaCheckLogin
     @SaCheckRole("admin")
     @Operation(summary = "更新用户角色", description = "更新指定用户的角色（仅管理员）")
     public BaseResponse<Boolean> updateUserRole(@Valid @RequestBody UpdateUserRoleRequest updateUserRoleRequest) {
         boolean result = userService.updateUserRole(
                 updateUserRoleRequest.getUserId(), 
                 updateUserRoleRequest.getNewRole());
         return ResultUtils.success(result);
     }

     /**
      * 获取用户角色列表
      *
      * @param userId 用户ID
      * @return 角色列表
      */
     @GetMapping("/roles/{userId}")
     @SaCheckLogin
     @SaCheckPermission(PermissionConstant.User.ADMIN)
     @Operation(summary = "获取用户角色", description = "获取指定用户的角色列表")
     public BaseResponse<List<String>> getUserRoles(@PathVariable Long userId) {
         List<String> roles = userService.getUserRoles(userId);
         return ResultUtils.success(roles);
     }

     /**
      * 获取用户权限列表
      *
      * @param userId 用户ID
      * @return 权限列表
      */
     @GetMapping("/permissions/{userId}")
     @SaCheckLogin
     @SaCheckPermission(PermissionConstant.User.ADMIN)
     @Operation(summary = "获取用户权限", description = "获取指定用户的权限列表")
     public BaseResponse<List<String>> getUserPermissions(@PathVariable Long userId) {
         List<String> permissions = userService.getUserPermissions(userId);
         return ResultUtils.success(permissions);
     }

     /**
      * 获取当前用户的角色和权限
      *
      * @return 角色和权限信息
      */
     @GetMapping("/my-permissions")
     @SaCheckLogin
     @Operation(summary = "获取我的权限", description = "获取当前登录用户的角色和权限")
     public BaseResponse<Map<String, Object>> getMyPermissions() {
         User loginUser = userService.getLoginUser();
         
         Map<String, Object> result = new HashMap<>();
         result.put("roles", userService.getUserRoles(loginUser.getId()));
         result.put("permissions", userService.getUserPermissions(loginUser.getId()));
         
         return ResultUtils.success(result);
     }
 }
