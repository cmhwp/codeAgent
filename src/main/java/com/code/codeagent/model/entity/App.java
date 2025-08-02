package com.code.codeagent.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用实体类
 *
 * @author CodeAgent
 * @since 2024-12-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("app")
@Schema(name = "App", description = "应用")
public class App implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "应用名称")
    @TableField("appName")
    private String appName;

    @Schema(description = "应用封面")
    @TableField("cover")
    private String cover;

    @Schema(description = "应用初始化的 prompt")
    @TableField("initPrompt")
    private String initPrompt;

    @Schema(description = "代码生成类型")
    @TableField("codeGenType")
    private String codeGenType;

    @Schema(description = "部署标识")
    @TableField("deployKey")
    private String deployKey;

    @Schema(description = "部署时间")
    @TableField("deployedTime")
    private LocalDateTime deployedTime;

    @Schema(description = "优先级")
    @TableField("priority")
    private Integer priority;

    @Schema(description = "创建用户id")
    @TableField("userId")
    private Long userId;

    @Schema(description = "编辑时间")
    @TableField("editTime")
    private LocalDateTime editTime;

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