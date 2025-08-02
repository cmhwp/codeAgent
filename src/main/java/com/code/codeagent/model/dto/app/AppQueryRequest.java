package com.code.codeagent.model.dto.app;

import com.code.codeagent.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 应用查询请求
 *
 * @author CodeAgent
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "AppQueryRequest", description = "应用查询请求")
public class AppQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "应用ID")
    private Long id;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用初始化的 prompt")
    private String initPrompt;

    @Schema(description = "代码生成类型：html-原生HTML模式，multi_file-原生多文件模式")
    private String codeGenType;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "创建用户ID")
    private Long userId;

    @Schema(description = "搜索关键词")
    private String searchText;
}