package com.code.codeagent.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * 多文件代码生成结果
 *
 * @author CodeAgent
 */
@Description("生成多文件代码的结果")
@Data
public class MultiFileCodeResult {

    @Description("HTML代码")
    private String htmlCode;

    @Description("CSS代码")
    private String cssCode;

    @Description("JavaScript代码")
    private String jsCode;

    @Description("生成代码的描述")
    private String description;
}