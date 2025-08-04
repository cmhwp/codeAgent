package com.code.codeagent.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 对话历史消息类型枚举
 *
 * @author CodeAgent
 */
@Getter
@AllArgsConstructor
public enum ChatHistoryMessageTypeEnum {

    USER("用户", "user"),
    AI("AI", "ai");

    private final String text;
    private final String value;

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static ChatHistoryMessageTypeEnum getEnumByValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        for (ChatHistoryMessageTypeEnum anEnum : ChatHistoryMessageTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}