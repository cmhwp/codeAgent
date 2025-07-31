package com.code.codeagent.core;

import com.code.codeagent.ai.AiCodeGeneratorService;
import com.code.codeagent.ai.model.HtmlCodeResult;
import com.code.codeagent.ai.model.MultiFileCodeResult;
import com.code.codeagent.exception.BusinessException;
import com.code.codeagent.exception.ErrorCode;
import com.code.codeagent.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 * 使用门面模式统一管理代码生成和文件保存逻辑
 *
 * @author CodeAgent
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        
        log.info("开始生成代码，类型：{}，用户消息：{}", codeGenTypeEnum.getText(), userMessage);
        
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                log.error(errorMessage);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 生成 HTML 模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        try {
            log.info("开始生成HTML代码");
            HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
            File savedDir = CodeFileSaver.saveHtmlCodeResult(result);
            log.info("HTML代码生成并保存成功，保存路径：{}", savedDir.getAbsolutePath());
            return savedDir;
        } catch (Exception e) {
            log.error("HTML代码生成失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码生成失败：" + e.getMessage());
        }
    }

    /**
     * 生成多文件模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        try {
            log.info("开始生成多文件代码");
            MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
            File savedDir = CodeFileSaver.saveMultiFileCodeResult(result);
            log.info("多文件代码生成并保存成功，保存路径：{}", savedDir.getAbsolutePath());
            return savedDir;
        } catch (Exception e) {
            log.error("多文件代码生成失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "多文件代码生成失败：" + e.getMessage());
        }
    }
}