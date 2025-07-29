package com.code.codeagent.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.code.codeagent.constant.MailConstant;
import com.code.codeagent.constant.UserConstant;
import com.code.codeagent.model.dto.UserUpdateMyRequest;
import com.code.codeagent.exception.BusinessException;
import com.code.codeagent.exception.ErrorCode;
import com.code.codeagent.service.MailService;
import com.code.codeagent.mapper.UserMapper;
import com.code.codeagent.model.entity.User;
import com.code.codeagent.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用户服务实现类
 *
 * @author CodeAgent
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private MailService mailService;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String userName, String userEmail) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】'；：\"\"'。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }
        
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            
            // 2. 加密
            String encryptPassword = DigestUtil.md5Hex((UserConstant.SALT + userPassword).getBytes());
            
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserRole(UserConstant.DEFAULT_ROLE);
            user.setUserStatus(UserConstant.UserStatus.NORMAL);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setLastLoginTime(LocalDateTime.now());
            
            // 设置可选字段
            if (userName != null && !userName.trim().isEmpty()) {
                user.setUserName(userName.trim());
            }
            if (userEmail != null && !userEmail.trim().isEmpty()) {
                user.setUserEmail(userEmail.trim());
            }
            
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public User userLogin(String userAccount, String userPassword) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        
        // 2. 加密
        String encryptPassword = DigestUtil.md5Hex((UserConstant.SALT + userPassword).getBytes());
        
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        
        // 检查用户状态
        if (user.getUserStatus() == UserConstant.UserStatus.DISABLED) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已被禁用");
        }
        
        // 3. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        this.updateById(user);
        
        // 4. Sa-Token 登录
        StpUtil.login(user.getId());
        
        // 5. 返回脱敏的用户信息
        return getSafetyUser(user);
    }

    @Override
    public User getLoginUser() {
        // 判断是否已登录
        Object userObj = StpUtil.getLoginIdDefaultNull();
        User currentUser = null;
        if (userObj != null) {
            long userId = Long.parseLong(userObj.toString());
            currentUser = this.getById(userId);
            if (currentUser == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
        }
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public User getLoginUserPermitNull() {
        // 先判断是否已登录
        Object userObj = StpUtil.getLoginIdDefaultNull();
        User currentUser = null;
        if (userObj != null) {
            long userId = Long.parseLong(userObj.toString());
            currentUser = this.getById(userId);
        }
        return currentUser;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserConstant.ADMIN_ROLE.equals(user.getUserRole());
    }

    @Override
    public boolean isAdmin() {
        User user = getLoginUserPermitNull();
        return isAdmin(user);
    }

    @Override
    public boolean userLogout() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // Sa-Token 注销
        StpUtil.logout();
        return true;
    }

    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        BeanUtils.copyProperties(originUser, safetyUser);
        safetyUser.setUserPassword(null);
        safetyUser.setIsDelete(null);
        return safetyUser;
    }

    @Override
    public boolean updateMyUser(User loginUser, UserUpdateMyRequest userUpdateMyRequest) {
        User user = new User();
        user.setId(loginUser.getId());
        
        // 只更新有值的字段
        if (StrUtil.isNotBlank(userUpdateMyRequest.getUserName())) {
            user.setUserName(userUpdateMyRequest.getUserName().trim());
        }
        if (StrUtil.isNotBlank(userUpdateMyRequest.getUserAvatar())) {
            user.setUserAvatar(userUpdateMyRequest.getUserAvatar().trim());
        }
        if (StrUtil.isNotBlank(userUpdateMyRequest.getUserProfile())) {
            user.setUserProfile(userUpdateMyRequest.getUserProfile().trim());
        }
        if (StrUtil.isNotBlank(userUpdateMyRequest.getUserEmail())) {
            // 检查邮箱是否已被其他用户使用
            String email = userUpdateMyRequest.getUserEmail().trim();
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userEmail", email);
            queryWrapper.ne("id", loginUser.getId());
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已被其他用户使用");
            }
            user.setUserEmail(email);
        }
        
        return this.updateById(user);
    }

    @Override
    public boolean bindEmail(User loginUser, String email, String code) {
        // 1. 参数校验
        if (StrUtil.hasBlank(email, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱或验证码不能为空");
        }

        // 2. 验证邮箱验证码
        if (!mailService.verifyCode(email, code, MailConstant.Purpose.BIND_EMAIL)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误或已过期");
        }

        // 3. 检查邮箱是否已被其他用户使用
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userEmail", email);
        queryWrapper.ne("id", loginUser.getId());
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已被其他用户使用");
        }

        // 4. 更新用户邮箱
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserEmail(email);
        
        boolean result = this.updateById(user);
        if (result) {
            log.info("用户 {} 绑定邮箱成功：{}", loginUser.getId(), email);
        }
        
        return result;
    }

    @Override
    public boolean changePassword(User loginUser, String oldPassword, String newPassword) {
        // 1. 参数校验
        if (StrUtil.hasBlank(oldPassword, newPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能为空");
        }

        if (newPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码长度不能少于8位");
        }

        // 2. 验证旧密码
        String encryptOldPassword = DigestUtil.md5Hex((UserConstant.SALT + oldPassword).getBytes());
        if (!encryptOldPassword.equals(loginUser.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前密码错误");
        }

        // 3. 检查新旧密码不能相同
        if (oldPassword.equals(newPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码不能与当前密码相同");
        }

        // 4. 加密新密码
        String encryptNewPassword = DigestUtil.md5Hex((UserConstant.SALT + newPassword).getBytes());

        // 5. 更新密码
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserPassword(encryptNewPassword);
        
        boolean result = this.updateById(user);
        if (result) {
            log.info("用户 {} 修改密码成功", loginUser.getId());
        }
        
        return result;
    }

    @Override
    public boolean resetPassword(String email, String code, String newPassword) {
        // 1. 参数校验
        if (StrUtil.hasBlank(email, code, newPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }

        if (newPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能少于8位");
        }

        // 2. 验证邮箱验证码
        if (!mailService.verifyCode(email, code, MailConstant.Purpose.RESET_PASSWORD)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误或已过期");
        }

        // 3. 查找用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userEmail", email);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱未绑定任何用户");
        }

        // 4. 加密新密码
        String encryptPassword = DigestUtil.md5Hex((UserConstant.SALT + newPassword).getBytes());

        // 5. 更新密码
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setUserPassword(encryptPassword);
        
        boolean result = this.updateById(updateUser);
        if (result) {
            log.info("用户 {} 重置密码成功", user.getId());
        }
        
        return result;
    }
}
