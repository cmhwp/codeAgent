package com.code.codeagent.service;

import com.code.codeagent.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.code.codeagent.model.dto.user.UserUpdateMyRequest;
import com.code.codeagent.model.dto.user.UserQueryRequest;
import com.code.codeagent.model.dto.user.UserAdminUpdateRequest;
import com.code.codeagent.model.dto.user.BatchUserOperationRequest;
import com.code.codeagent.model.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Map;

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
    boolean updateMyUser(User loginUser, UserUpdateMyRequest userUpdateMyRequest);

    /**
     * 绑定邮箱
     *
     * @param loginUser 当前登录用户
     * @param email 邮箱地址
     * @param code 验证码
     * @return 是否成功
     */
    boolean bindEmail(User loginUser, String email, String code);

    /**
     * 修改密码
     *
     * @param loginUser 当前登录用户
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean changePassword(User loginUser, String oldPassword, String newPassword);

    /**
     * 重置密码
     *
     * @param email 邮箱地址
     * @param code 验证码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPassword(String email, String code, String newPassword);

    /**
     * 更新用户角色（仅管理员）
     *
     * @param userId 用户ID
     * @param newRole 新角色
     * @return 是否成功
     */
    boolean updateUserRole(Long userId, String newRole);

    /**
     * 获取用户角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 获取用户查询条件
     *
     * @param userQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 管理员更新用户信息
     *
     * @param userAdminUpdateRequest 更新请求
     * @return 是否成功
     */
    boolean updateUserByAdmin(UserAdminUpdateRequest userAdminUpdateRequest);

    /**
     * 管理员删除用户
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUserByAdmin(Long userId);

    /**
     * 批量操作用户
     *
     * @param batchUserOperationRequest 批量操作请求
     * @return 操作结果
     */
    Map<String, Object> batchOperateUsers(BatchUserOperationRequest batchUserOperationRequest);

    /**
     * 强制用户下线
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean kickoutUser(Long userId);

    /**
     * 获取用户统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getUserStats();

    /**
     * 封禁用户
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean banUser(Long userId);

    /**
     * 解封用户
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean unbanUser(Long userId);

    /**
     * 获取用户VO对象
     *
     * @param user 用户实体
     * @return 用户VO
     */
    UserVO getUserVO(User user);

    /**
     * 获取用户VO列表
     *
     * @param userList 用户列表
     * @return 用户VO列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 校验用户参数
     *
     * @param user 用户对象
     * @param add 是否为新增
     */
    void validUser(User user, boolean add);
}
