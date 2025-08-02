package com.code.codeagent.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 应用更新请求（管理员）
 *
 * @author CodeAgent
 */
@Data
@Schema(name = "AppAdminUpdateRequest", description = "应用更新请求（管理员）")
public class AppAdminUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "应用ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "应用ID不能为空")
    private Long id;

    @Schema(description = "应用名称")
    @Size(max = 100, message = "应用名称长度不能超过100字符")
    private String appName;

    @Schema(description = "应用封面URL")
    @Size(max = 500, message = "应用封面URL长度不能超过500字符")
    private String cover;

    @Schema(description = "应用初始化的 prompt")
    @Size(max = 2000, message = "初始化 prompt 长度不能超过2000字符")
    private String initPrompt;

    @Schema(description = "代码生成类型：html-原生HTML模式，multi_file-原生多文件模式")
    private String codeGenType;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "创建用户ID")
    private Long userId;
}