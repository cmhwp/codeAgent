package com.code.codeagent.model.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 *
 * @author CodeAgent
 */
@Getter
public enum UserRoleEnum {

    USER("user", "普通用户", "基础用户权限"),
    ADMIN("admin", "管理员", "完整管理权限"),
    BAN("ban", "被封号", "无任何权限");

    private final String value;

    private final String text;

    private final String description;

    UserRoleEnum(String value, String text, String description) {
        this.value = value;
        this.text = text;
        this.description = description;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 值
     * @return 枚举
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    /**
     * 检查是否为管理员角色
     *
     * @param role 角色值
     * @return 是否为管理员
     */
    public static boolean isAdmin(String role) {
        return ADMIN.getValue().equals(role);
    }

    /**
     * 检查是否为普通用户角色
     *
     * @param role 角色值
     * @return 是否为普通用户
     */
    public static boolean isUser(String role) {
        return USER.getValue().equals(role);
    }

    /**
     * 检查是否为被封号角色
     *
     * @param role 角色值
     * @return 是否被封号
     */
    public static boolean isBan(String role) {
        return BAN.getValue().equals(role);
    }
} 