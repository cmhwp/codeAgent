package com.code.codeagent.service;

import com.code.codeagent.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户服务接口
 *
 * @author CodeAgent
 * @since 2024-12-19
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param userName      用户昵称（可选）
     * @param userEmail     用户邮箱（可选）
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String userName, String userEmail);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword);

    /**
     * 获取当前登录用户
     *
     * @return 当前登录用户
     */
    User getLoginUser();

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return 脱敏用户信息
     */
    User getLoginUserPermitNull();

    /**
     * 是否为管理员
     *
     * @param user 用户
     * @return 是否为管理员
     */
    boolean isAdmin(User user);

    /**
     * 是否为管理员
     *
     * @return 是否为管理员
     */
    boolean isAdmin();

    /**
     * 用户注销
     *
     * @return 是否成功
     */
    boolean userLogout();

    /**
     * 获取脱敏的用户信息
     *
     * @param originUser 原始用户信息
     * @return 脱敏用户信息
     */
    User getSafetyUser(User originUser);

    /**
     * 更新个人信息
     *
     * @param loginUser 当前登录用户
     * @param userUpdateMyRequest 更新请求
     * @return 是否成功
     */
    boolean updateMyUser(User loginUser, com.code.codeagent.model.dto.UserUpdateMyRequest userUpdateMyRequest);
}
