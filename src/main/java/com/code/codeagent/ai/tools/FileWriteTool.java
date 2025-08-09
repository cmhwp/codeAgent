package com.code.codeagent.ai.tools;

import com.code.codeagent.constant.AppConstant;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;


import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
public class FileWriteTool {
    @Tool("写入文件到指定路径")
    public String fileWriteTool(@P("文件的相对路径")String relativePath, @P("要写入的文件内容")String content, @ToolMemoryId Long appId) {
        log.info("写入文件到指定路径: {}, 文件内容: {}, 应用ID: {}", relativePath, content, appId);
        try {
            Path path = Paths.get(relativePath);
            if (!path.isAbsolute()) {
                String projectDirName = "vue_project_" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                path = projectRoot.resolve(relativePath);
            }
            Path parentDir = path.getParent();
            if (parentDir != null) {    
                Files.createDirectories(parentDir);
            }
            Files.write(path, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("成功写入文件: {}", path.toAbsolutePath());
            return "文件写入成功: " + relativePath;
        } catch (IOException e) {
            String errorMessage = "文件写入失败: " + relativePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }
}
