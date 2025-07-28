package com.code.codeagent.model.enums;

import lombok.Getter;

/**
 * 用户状态枚举
 *
 * @author CodeAgent
 */
@Getter
public enum UserStatusEnum {

    NORMAL(0, "正常"),
    DISABLED(1, "禁用");

    private final int value;

    private final String text;

    UserStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 值
     * @return 枚举
     */
    public static UserStatusEnum getEnumByValue(int value) {
        for (UserStatusEnum anEnum : UserStatusEnum.values()) {
            if (anEnum.value == value) {
                return anEnum;
            }
        }
        return null;
    }
} 