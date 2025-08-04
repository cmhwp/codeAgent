package com.code.codeagent.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史实体类
 *
 * @author CodeAgent
 * @since 2024-12-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_history")
@Schema(name = "ChatHistory", description = "对话历史")
public class ChatHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "消息")
    @TableField("message")
    private String message;

    @Schema(description = "消息类型：user/ai")
    @TableField("messageType")
    private String messageType;

    @Schema(description = "应用id")
    @TableField("appId")
    private Long appId;

    @Schema(description = "创建用户id")
    @TableField("userId")
    private Long userId;

    @Schema(description = "父消息id（用于上下文关联）")
    @TableField("parentId")
    private Long parentId;

    @Schema(description = "创建时间")
    @TableField(value = "createTime", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "updateTime", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField("isDelete")
    @TableLogic
    private Integer isDelete;
}