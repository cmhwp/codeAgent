package com.code.codeagent.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.code.codeagent.constant.UserConstant;
import com.code.codeagent.model.entity.User;
import com.code.codeagent.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限认证接口实现类
 * 用于告诉 Sa-Token 框架，当前用户具有哪些角色和权限
 *
 * @author CodeAgent
 */
@Component
@Slf4j
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private UserService userService;

    /**
     * 返回指定loginId对应用户的角色标识集合
     *
     * @param loginId   用户id
     * @param loginType 登录类型
     * @return 角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roleList = new ArrayList<>();
        
        try {
            // 根据loginId获取用户信息
            Long userId = Long.parseLong(loginId.toString());
            User user = userService.getById(userId);
            
            if (user != null && user.getUserRole() != null) {
                roleList.add(user.getUserRole());
                log.debug("用户 {} 的角色列表: {}", loginId, roleList);
            }
        } catch (Exception e) {
            log.error("获取用户角色失败，loginId: {}", loginId, e);
        }
        
        return roleList;
    }

    /**
     * 返回指定loginId对应用户的权限标识集合
     *
     * @param loginId   用户id
     * @param loginType 登录类型
     * @return 权限标识集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> permissionList = new ArrayList<>();
        
        try {
            // 根据loginId获取用户信息
            Long userId = Long.parseLong(loginId.toString());
            User user = userService.getById(userId);
            
            if (user != null && user.getUserRole() != null) {
                String userRole = user.getUserRole();
                
                // 根据角色分配权限
                switch (userRole) {
                    case UserConstant.ADMIN_ROLE:
                        // 管理员拥有所有权限
                        permissionList.add("user:read");
                        permissionList.add("user:write");
                        permissionList.add("user:delete");
                        permissionList.add("user:admin");
                        permissionList.add("system:manage");
                        break;
                        
                    case UserConstant.DEFAULT_ROLE:
                        // 普通用户权限
                        permissionList.add("user:read");
                        permissionList.add("user:write");
                        break;
                        
                    case UserConstant.BAN_ROLE:
                        // 被封号用户没有权限
                        break;
                        
                    default:
                        // 默认权限
                        permissionList.add("user:read");
                        break;
                }
                
                log.debug("用户 {} 的权限列表: {}", loginId, permissionList);
            }
        } catch (Exception e) {
            log.error("获取用户权限失败，loginId: {}", loginId, e);
        }
        
        return permissionList;
    }
} 