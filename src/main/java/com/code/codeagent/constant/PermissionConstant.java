package com.code.codeagent.constant;

/**
 * 权限常量
 *
 * @author CodeAgent
 */
public interface PermissionConstant {

    /**
     * 用户相关权限
     */
    interface User {
        /**
         * 查看用户信息
         */
        String READ = "user:read";

        /**
         * 修改用户信息
         */
        String WRITE = "user:write";

        /**
         * 删除用户
         */
        String DELETE = "user:delete";

        /**
         * 用户管理（管理员权限）
         */
        String ADMIN = "user:admin";
    }

    /**
     * 系统相关权限
     */
    interface System {
        /**
         * 系统管理
         */
        String MANAGE = "system:manage";

        /**
         * 系统配置
         */
        String CONFIG = "system:config";

        /**
         * 系统监控
         */
        String MONITOR = "system:monitor";
    }

    /**
     * 邮件相关权限
     */
    interface Mail {
        /**
         * 发送邮件
         */
        String SEND = "mail:send";

        /**
         * 邮件管理
         */
        String MANAGE = "mail:manage";
    }
} 