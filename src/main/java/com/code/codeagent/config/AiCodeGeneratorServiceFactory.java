package com.code.codeagent.config;

import com.code.codeagent.ai.AiCodeGeneratorService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

/**
 * AI 代码生成服务工厂配置类
 *
 * @author CodeAgent
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    /**
     * 创建 AI 代码生成服务实例
     *
     * @return AI代码生成服务
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        log.info("正在初始化 AI 代码生成服务");
        log.info("chatModel: {}", chatModel != null ? "已注入" : "为空");
        log.info("streamingChatModel: {}", streamingChatModel != null ? "已注入" : "为空");
        
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();
    }
}