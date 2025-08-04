package com.code.codeagent.model.dto.chathistory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 对话历史添加请求
 *
 * @author CodeAgent
 */
@Data
@Schema(name = "ChatHistoryAddRequest", description = "对话历史添加请求")
public class ChatHistoryAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "消息内容不能为空")
    private String message;

    @Schema(description = "消息类型：user/ai", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "消息类型不能为空")
    private String messageType;

    @Schema(description = "应用id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "应用id不能为空")
    private Long appId;

    @Schema(description = "父消息id（用于上下文关联）")
    private Long parentId;
}