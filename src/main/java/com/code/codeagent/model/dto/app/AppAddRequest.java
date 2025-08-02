package com.code.codeagent.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 应用创建请求
 *
 * @author CodeAgent
 */
@Data
@Schema(name = "AppAddRequest", description = "应用创建请求")
public class AppAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "应用初始化的 prompt", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "应用初始化的 prompt 不能为空")
    @Size(max = 2000, message = "初始化 prompt 长度不能超过2000字符")
    private String initPrompt;

    @Schema(description = "应用封面URL")
    @Size(max = 500, message = "应用封面URL长度不能超过500字符")
    private String cover;

    @Schema(description = "代码生成类型：html-原生HTML模式，multi_file-原生多文件模式")
    private String codeGenType;
}