package com.code.codeagent.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.code.codeagent.common.BaseResponse;
import com.code.codeagent.common.ResultUtils;
import com.code.codeagent.core.AiCodeGeneratorFacade;
import com.code.codeagent.model.dto.CodeGenerateRequest;
import com.code.codeagent.model.enums.CodeGenTypeEnum;
import com.code.codeagent.model.vo.CodeGenerateVO;
import com.code.codeagent.exception.BusinessException;
import com.code.codeagent.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.io.File;

/**
 * AI 代码生成控制器
 *
 * @author CodeAgent
 */
@RestController
@RequestMapping("/code-generator")
@Tag(name = "代码生成接口", description = "AI代码生成相关接口")
@Slf4j
public class CodeGeneratorController {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    /**
     * 生成代码
     *
     * @param codeGenerateRequest 代码生成请求
     * @return 生成结果
     */
    @PostMapping("/generate")
    @SaCheckLogin
    @Operation(summary = "生成代码", description = "根据用户需求生成前端代码")
    public BaseResponse<CodeGenerateVO> generateCode(@Valid @RequestBody CodeGenerateRequest codeGenerateRequest) {
        String userMessage = codeGenerateRequest.getUserMessage();
        String codeGenType = codeGenerateRequest.getCodeGenType();
        
        // 参数校验
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的生成类型：" + codeGenType);
        }
        
        try {
            // 生成并保存代码
            File savedDir = aiCodeGeneratorFacade.generateAndSaveCode(userMessage, codeGenTypeEnum);
            
            // 构建返回结果
            CodeGenerateVO result = new CodeGenerateVO();
            result.setFilePath(savedDir.getAbsolutePath());
            result.setCodeGenType(codeGenTypeEnum.getValue());
            result.setDescription("代码生成成功");
            result.setSuccess(true);
            result.setTimestamp(System.currentTimeMillis());
            
            log.info("代码生成成功，类型：{}，保存路径：{}", codeGenTypeEnum.getText(), savedDir.getAbsolutePath());
            return ResultUtils.success(result);
            
        } catch (Exception e) {
            log.error("代码生成失败", e);
            
            CodeGenerateVO result = new CodeGenerateVO();
            result.setCodeGenType(codeGenTypeEnum.getValue());
            result.setDescription("代码生成失败：" + e.getMessage());
            result.setSuccess(false);
            result.setTimestamp(System.currentTimeMillis());
            
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "代码生成失败", result);
        }
    }

    /**
     * 获取支持的生成类型
     *
     * @return 生成类型列表
     */
    @GetMapping("/types")
    @SaCheckLogin
    @Operation(summary = "获取生成类型", description = "获取支持的代码生成类型列表")
    public BaseResponse<CodeGenTypeEnum[]> getCodeGenTypes() {
        return ResultUtils.success(CodeGenTypeEnum.values());
    }

    /**
     * 健康检查
     *
     * @return 服务状态
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查AI代码生成服务是否正常")
    public BaseResponse<String> health() {
        return ResultUtils.success("AI代码生成服务运行正常");
    }
}