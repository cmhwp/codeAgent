package com.code.codeagent.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.code.codeagent.constant.MailConstant;
import com.code.codeagent.constant.PermissionConstant;
import com.code.codeagent.constant.UserConstant;
import com.code.codeagent.model.dto.user.UserUpdateMyRequest;
import com.code.codeagent.model.dto.user.UserQueryRequest;
import com.code.codeagent.model.dto.user.UserAdminUpdateRequest;
import com.code.codeagent.model.dto.user.BatchUserOperationRequest;
import com.code.codeagent.model.vo.UserVO;
import com.code.codeagent.model.enums.UserRoleEnum;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    @Override
    public boolean updateUserRole(Long userId, String newRole) {
        // 1. 校验参数
        if (userId == null || StrUtil.isBlank(newRole)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }

        // 2. 校验角色是否有效
        UserRoleEnum roleEnum = UserRoleEnum.getEnumByValue(newRole);
        if (roleEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的角色");
        }

        // 3. 校验用户是否存在
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        // 4. 更新角色
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setUserRole(newRole);
        
        boolean result = this.updateById(updateUser);
        if (result) {
            log.info("用户 {} 角色更新成功：{} -> {}", userId, user.getUserRole(), newRole);
        }
        
        return result;
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        List<String> roles = new ArrayList<>();
        User user = this.getById(userId);
        if (user != null && user.getUserRole() != null) {
            roles.add(user.getUserRole());
        }
        return roles;
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        List<String> permissions = new ArrayList<>();
        User user = this.getById(userId);
        
        if (user != null && user.getUserRole() != null) {
            String userRole = user.getUserRole();
            
            // 根据角色分配权限
            switch (userRole) {
                case UserConstant.ADMIN_ROLE:
                    // 管理员拥有所有权限
                    permissions.add(PermissionConstant.User.READ);
                    permissions.add(PermissionConstant.User.WRITE);
                    permissions.add(PermissionConstant.User.DELETE);
                    permissions.add(PermissionConstant.User.ADMIN);
                    permissions.add(PermissionConstant.System.MANAGE);
                    permissions.add(PermissionConstant.Mail.SEND);
                    permissions.add(PermissionConstant.Mail.MANAGE);
                    break;
                    
                case UserConstant.DEFAULT_ROLE:
                    // 普通用户权限
                    permissions.add(PermissionConstant.User.READ);
                    permissions.add(PermissionConstant.User.WRITE);
                    permissions.add(PermissionConstant.Mail.SEND);
                    break;
                    
                case UserConstant.BAN_ROLE:
                    // 被封号用户没有权限
                    break;
                    
                default:
                    // 默认权限
                    permissions.add(PermissionConstant.User.READ);
                    break;
            }
        }
        
        return permissions;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        
        if (userQueryRequest == null) {
            return queryWrapper;
        }
        
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userEmail = userQueryRequest.getUserEmail();
        String userRole = userQueryRequest.getUserRole();
        Integer userStatus = userQueryRequest.getUserStatus();
        String searchText = userQueryRequest.getSearchText();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        
        // 精确匹配
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.eq(userStatus != null, "userStatus", userStatus);
        
        // 模糊匹配
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userEmail), "userEmail", userEmail);
        
        // 搜索关键词（账号、昵称、邮箱）
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw
                .like("userAccount", searchText)
                .or().like("userName", searchText)
                .or().like("userEmail", searchText)
            );
        }
        
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), 
                "ascend".equals(sortOrder), sortField);
        
        return queryWrapper;
    }

    @Override
    public boolean updateUserByAdmin(UserAdminUpdateRequest userAdminUpdateRequest) {
        Long id = userAdminUpdateRequest.getId();
        
        // 判断用户是否存在
        User oldUser = this.getById(id);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        
        User user = new User();
        BeanUtils.copyProperties(userAdminUpdateRequest, user);
        user.setId(id);
        user.setEditTime(LocalDateTime.now());
        
        // 参数校验
        validUser(user, false);
        
        // 如果修改了账号，检查是否重复
        if (StrUtil.isNotBlank(user.getUserAccount()) && 
            !user.getUserAccount().equals(oldUser.getUserAccount())) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", user.getUserAccount());
            queryWrapper.ne("id", id);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
            }
        }
        
        // 如果修改了邮箱，检查是否重复
        if (StrUtil.isNotBlank(user.getUserEmail()) && 
            !user.getUserEmail().equals(oldUser.getUserEmail())) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userEmail", user.getUserEmail());
            queryWrapper.ne("id", id);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱已被使用");
            }
        }
        
        return this.updateById(user);
    }

    @Override
    public boolean deleteUserByAdmin(Long userId) {
        // 判断用户是否存在
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        
        // 不能删除管理员
        if (isAdmin(user)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "不能删除管理员账户");
        }
        
        // 强制用户下线
        try {
            StpUtil.kickout(userId);
        } catch (Exception e) {
            log.warn("强制用户下线失败: {}", e.getMessage());
        }
        
        return this.removeById(userId);
    }

    @Override
    public Map<String, Object> batchOperateUsers(BatchUserOperationRequest batchUserOperationRequest) {
        List<Long> userIds = batchUserOperationRequest.getUserIds();
        String operation = batchUserOperationRequest.getOperation();
        String parameter = batchUserOperationRequest.getParameter();
        
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (Long userId : userIds) {
            try {
                User user = this.getById(userId);
                if (user == null) {
                    errors.add("用户 " + userId + " 不存在");
                    failCount++;
                    continue;
                }
                
                // 不能操作管理员
                if (isAdmin(user) && !"role".equals(operation)) {
                    errors.add("用户 " + userId + " 是管理员，不能执行此操作");
                    failCount++;
                    continue;
                }
                
                boolean success = false;
                switch (operation) {
                    case "delete":
                        success = deleteUserByAdmin(userId);
                        break;
                    case "ban":
                        success = banUser(userId);
                        break;
                    case "unban":
                        success = unbanUser(userId);
                        break;
                    case "role":
                        if (StrUtil.isNotBlank(parameter)) {
                            success = updateUserRole(userId, parameter);
                        } else {
                            errors.add("用户 " + userId + " 角色参数不能为空");
                            failCount++;
                            continue;
                        }
                        break;
                    default:
                        errors.add("用户 " + userId + " 不支持的操作类型");
                        failCount++;
                        continue;
                }
                
                if (success) {
                    successCount++;
                } else {
                    errors.add("用户 " + userId + " 操作失败");
                    failCount++;
                }
            } catch (Exception e) {
                errors.add("用户 " + userId + " 操作异常: " + e.getMessage());
                failCount++;
            }
        }
        
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        
        return result;
    }

    @Override
    public boolean kickoutUser(Long userId) {
        try {
            StpUtil.kickout(userId);
            log.info("强制用户 {} 下线成功", userId);
            return true;
        } catch (Exception e) {
            log.error("强制用户 {} 下线失败: {}", userId, e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "强制下线失败");
        }
    }

    @Override
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总用户数
        long totalUsers = this.count();
        stats.put("totalUsers", totalUsers);
        
        // 活跃用户数（状态为正常）
        QueryWrapper<User> activeWrapper = new QueryWrapper<>();
        activeWrapper.eq("userStatus", UserConstant.UserStatus.NORMAL);
        long activeUsers = this.count(activeWrapper);
        stats.put("activeUsers", activeUsers);
        
        // 被封禁用户数（状态为禁用）
        QueryWrapper<User> bannedWrapper = new QueryWrapper<>();
        bannedWrapper.eq("userStatus", UserConstant.UserStatus.DISABLED);
        long bannedUsers = this.count(bannedWrapper);
        stats.put("bannedUsers", bannedUsers);
        
        // 管理员数量
        QueryWrapper<User> adminWrapper = new QueryWrapper<>();
        adminWrapper.eq("userRole", UserConstant.ADMIN_ROLE);
        long adminUsers = this.count(adminWrapper);
        stats.put("adminUsers", adminUsers);
        
        // 普通用户数量
        QueryWrapper<User> normalWrapper = new QueryWrapper<>();
        normalWrapper.eq("userRole", UserConstant.DEFAULT_ROLE);
        long normalUsers = this.count(normalWrapper);
        stats.put("normalUsers", normalUsers);
        
        // 今日新注册用户数
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        QueryWrapper<User> todayWrapper = new QueryWrapper<>();
        todayWrapper.ge("createTime", todayStart);
        long todayNewUsers = this.count(todayWrapper);
        stats.put("todayNewUsers", todayNewUsers);
        
        return stats;
    }

    @Override
    public boolean banUser(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setUserStatus(UserConstant.UserStatus.DISABLED);
        user.setUserRole(UserConstant.BAN_ROLE);
        user.setEditTime(LocalDateTime.now());
        
        // 强制用户下线
        try {
            StpUtil.kickout(userId);
        } catch (Exception e) {
            log.warn("强制用户下线失败: {}", e.getMessage());
        }
        
        boolean result = this.updateById(user);
        if (result) {
            log.info("封禁用户 {} 成功", userId);
        }
        
        return result;
    }

    @Override
    public boolean unbanUser(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setUserStatus(UserConstant.UserStatus.NORMAL);
        user.setUserRole(UserConstant.DEFAULT_ROLE);
        user.setEditTime(LocalDateTime.now());
        
        boolean result = this.updateById(user);
        if (result) {
            log.info("解封用户 {} 成功", userId);
        }
        
        return result;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (userList == null || userList.isEmpty()) {
            return new ArrayList<>();
        }
        
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    @Override
    public void validUser(User user, boolean add) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户信息不能为空");
        }
        
        String userAccount = user.getUserAccount();
        String userName = user.getUserName();
        String userEmail = user.getUserEmail();
        String userRole = user.getUserRole();
        
        // 新增时必须填写账号
        if (add && StrUtil.isBlank(userAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号不能为空");
        }
        
        // 账号长度校验
        if (StrUtil.isNotBlank(userAccount)) {
            if (userAccount.length() < 4 || userAccount.length() > 16) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号长度必须在4-16字符之间");
            }
            
            // 账户不能包含特殊字符
            String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】'；：\"\"'。，、？]";
            Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
            if (matcher.find()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含特殊字符");
            }
        }
        
        // 昵称长度校验
        if (StrUtil.isNotBlank(userName) && userName.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户昵称长度不能超过50字符");
        }
        
        // 邮箱格式校验
        if (StrUtil.isNotBlank(userEmail)) {
            String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
            if (!Pattern.matches(emailPattern, userEmail)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
            }
        }
        
        // 角色校验
        if (StrUtil.isNotBlank(userRole)) {
            if (!UserConstant.ADMIN_ROLE.equals(userRole) && 
                !UserConstant.DEFAULT_ROLE.equals(userRole) && 
                !UserConstant.BAN_ROLE.equals(userRole)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户角色不合法");
            }
        }
    }
}
