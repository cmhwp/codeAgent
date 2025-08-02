package com.code.codeagent.model.dto.code;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 代码生成请求
 *
 * @author CodeAgent
 */
@Data
@Schema(name = "CodeGenerateRequest", description = "代码生成请求")
public class CodeGenerateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户消息/需求描述", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户消息不能为空")
    private String userMessage;

    @Schema(description = "生成类型：html-原生HTML模式，multi_file-原生多文件模式", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "生成类型不能为空")
    private String codeGenType;
}