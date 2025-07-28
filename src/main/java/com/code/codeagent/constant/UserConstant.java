package com.code.codeagent.constant;

/**
 * 用户常量
 *
 * @author CodeAgent
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    // endregion

    /**
     * 盐值，混淆密码
     */
    String SALT = "codeagent";

    /**
     * 用户状态
     */
    interface UserStatus {
        /**
         * 正常
         */
        int NORMAL = 0;

        /**
         * 禁用
         */
        int DISABLED = 1;
    }
}