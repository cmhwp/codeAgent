package com.code.codeagent.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 代码生成结果视图对象
 *
 * @author CodeAgent
 */
@Data
@Schema(name = "CodeGenerateVO", description = "代码生成结果")
public class CodeGenerateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "生成的代码文件保存路径")
    private String filePath;

    @Schema(description = "生成类型")
    private String codeGenType;

    @Schema(description = "生成描述")
    private String description;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "生成时间戳")
    private Long timestamp;
}