package com.code.codeagent.model.dto.chathistory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 对话历史重试请求
 *
 * @author CodeAgent
 */
@Data
@Schema(name = "ChatHistoryRetryRequest", description = "对话历史重试请求")
public class ChatHistoryRetryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户消息id（即需要重新生成AI回复的用户消息）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户消息id不能为空")
    private Long userMessageId;

    @Schema(description = "应用id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "应用id不能为空")
    private Long appId;
}