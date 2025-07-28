package com.code.codeagent.model.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 *
 * @author CodeAgent
 */
@Getter
public enum UserRoleEnum {

    USER("user", "普通用户"),
    ADMIN("admin", "管理员"),
    BAN("ban", "被封号");

    private final String value;

    private final String text;

    UserRoleEnum(String value, String text) {
        this.value = value;
        this.text = text;
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
} 