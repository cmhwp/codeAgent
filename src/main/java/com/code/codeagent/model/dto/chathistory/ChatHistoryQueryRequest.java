package com.code.codeagent.model.dto.chathistory;

import com.code.codeagent.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史查询请求
 *
 * @author CodeAgent
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "ChatHistoryQueryRequest", description = "对话历史查询请求")
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;

    @Schema(description = "消息内容")
    private String message;

    @Schema(description = "消息类型（user/ai）")
    private String messageType;

    @Schema(description = "应用id")
    private Long appId;

    @Schema(description = "创建用户id")
    private Long userId;

    @Schema(description = "父消息id")
    private Long parentId;

    @Schema(description = "游标查询 - 最后一条记录的创建时间，用于分页查询，获取早于此时间的记录")
    private LocalDateTime lastCreateTime;
}