package com.code.codeagent.config;

import com.code.codeagent.ai.AiCodeGeneratorService;
import com.code.codeagent.ai.tools.FileWriteTool;
import com.code.codeagent.exception.BusinessException;
import com.code.codeagent.exception.ErrorCode;
import com.code.codeagent.model.enums.CodeGenTypeEnum;
import com.code.codeagent.service.ChatHistoryService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.time.Duration;
import java.util.Map;

/**
 * AI代码生成器服务工厂
 * 使用 Caffeine 缓存来存储 AI 服务实例，避免重复构造
 *
 * @author CodeAgent
 * @since 2024-12-19
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    @Qualifier("reasoningStreamingChatModel")
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private StreamingChatModel reasoningStreamingChatModel;

    @Autowired(required = false)
    private ChatHistoryService chatHistoryService;

    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .recordStats()  // 启用统计
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，缓存键: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 根据 appId 获取缓存的服务实例（兼容旧逻辑）
     *
     * @param appId 应用ID
     * @return AI代码生成器服务实例
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }

    /**
     * 根据 appId 和代码生成类型获取缓存的服务实例
     *
     * @param appId 应用ID
     * @param codeGenType 代码生成类型
     * @return AI代码生成器服务实例
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        String cacheKey = buildCacheKey(appId, codeGenType);
        return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenType));
    }

    /**
     * 构建缓存键
     *
     * @param appId 应用ID
     * @param codeGenType 代码生成类型
     * @return 缓存键
     */
    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType.getValue();
    }

    /**
     * 创建新的 AI 服务实例
     *
     * @param appId 应用ID
     * @param codeGenType 代码生成类型
     * @return AI代码生成器服务实例
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        log.info("为 appId: {}, 代码类型: {} 创建新的 AI 服务实例", appId, codeGenType.getValue());
        
        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)  // 最大保存20条消息
                .build();
        
        // 从数据库中加载对话历史到记忆中（如果ChatHistoryService可用且appId有效）
        if (chatHistoryService != null && appId > 0) {
            try {
                chatHistoryService.getChatContext(appId, 20);
                log.debug("已为 appId: {} 加载对话历史", appId);
            } catch (Exception e) {
                log.warn("加载对话历史失败，appId: {}, 错误: {}", appId, e.getMessage());
            }
        } else if (appId <= 0) {
            log.debug("跳过加载对话历史，appId: {} 无效", appId);
        }
        switch (codeGenType) {
            case VUE_PROJECT -> {
                return AiServices.builder(AiCodeGeneratorService.class)
                        .chatModel(chatModel)
                        .streamingChatModel(reasoningStreamingChatModel)
                        .chatMemoryProvider(memoryId->chatMemory)
                        .tools(new FileWriteTool(CodeGenTypeEnum.VUE_PROJECT))
                        // 处理工具调用幻觉问题
                        .hallucinatedToolNameStrategy(toolExecutionRequest ->
                                ToolExecutionResultMessage.from(toolExecutionRequest,
                                        "Error: there is no tool called " + toolExecutionRequest.name())        
                        )
                        .build();
            }
            case REACT_PROJECT -> {
                return AiServices.builder(AiCodeGeneratorService.class)
                        .chatModel(chatModel)
                        .streamingChatModel(reasoningStreamingChatModel)
                        .chatMemoryProvider(memoryId->chatMemory)
                        .tools(new FileWriteTool(CodeGenTypeEnum.REACT_PROJECT))
                        // 处理工具调用幻觉问题
                        .hallucinatedToolNameStrategy(toolExecutionRequest ->
                                ToolExecutionResultMessage.from(toolExecutionRequest,
                                        "Error: there is no tool called " + toolExecutionRequest.name())        
                        )
                        .build();
            }
            case HTML, MULTI_FILE -> {
                return AiServices.builder(AiCodeGeneratorService.class)
                        .chatModel(chatModel)
                        .streamingChatModel(streamingChatModel)
                        .chatMemory(chatMemory)
                        .build();
            }
            default -> {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType.getValue());
            }
        }
    }

    /**
     * 创建默认的AI代码生成器服务Bean
     * 
     * @return AI代码生成器服务实例
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        log.info("正在初始化默认 AI 代码生成服务");
        log.info("chatModel: {}", chatModel != null ? "已注入" : "为空");
        log.info("streamingChatModel: {}", streamingChatModel != null ? "已注入" : "为空");
        log.info("redisChatMemoryStore: {}", redisChatMemoryStore != null ? "已注入" : "为空");
        log.info("reasoningStreamingChatModel: {}", reasoningStreamingChatModel != null ? "已注入" : "为空");   
        return getAiCodeGeneratorService(0L);  // 使用默认的 appId = 0
    }

    /**
     * 清除指定应用的缓存
     * 
     * @param appId 应用ID
     */
    public void evictCache(Long appId) {
        // 清除该应用所有类型的缓存
        for (CodeGenTypeEnum codeGenType : CodeGenTypeEnum.values()) {
            String cacheKey = buildCacheKey(appId, codeGenType);
            serviceCache.invalidate(cacheKey);
        }
        log.info("已清除 appId: {} 的所有AI服务缓存", appId);
    }

    /**
     * 清除指定应用和代码类型的缓存
     * 
     * @param appId 应用ID
     * @param codeGenType 代码生成类型
     */
    public void evictCache(Long appId, CodeGenTypeEnum codeGenType) {
        String cacheKey = buildCacheKey(appId, codeGenType);
        serviceCache.invalidate(cacheKey);
        log.info("已清除 appId: {}, 代码类型: {} 的AI服务缓存", appId, codeGenType.getValue());
    }

    /**
     * 清除所有缓存
     */
    public void evictAllCaches() {
        serviceCache.invalidateAll();
        log.info("已清除所有AI服务缓存");
    }

    /**
     * 预热缓存
     * 
     * @param appId 应用ID
     */
    public void warmupCache(Long appId) {
        for (CodeGenTypeEnum codeGenType : CodeGenTypeEnum.values()) {
            try {
                getAiCodeGeneratorService(appId, codeGenType);
                log.debug("已预热 appId: {}, 代码类型: {} 的缓存", appId, codeGenType.getValue());
            } catch (Exception e) {
                log.warn("预热缓存失败，appId: {}, 代码类型: {}, 错误: {}", 
                        appId, codeGenType.getValue(), e.getMessage());
            }
        }
        log.info("已完成 appId: {} 的缓存预热", appId);
    }

    /**
     * 检查应用是否属于指定用户（用于权限校验）
     * 
     * @param appId 应用ID
     * @param userId 用户ID
     * @return 是否属于该用户
     */
    public boolean isAppOwner(Long appId, Long userId) {
        // 这里可以添加实际的权限检查逻辑
        // 目前暂时返回 false，让上层调用者自行处理权限
        return false;
    }

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        var stats = serviceCache.stats();
        return Map.of(
                "size", serviceCache.estimatedSize(),
                "hitRate", String.format("%.2f%%", stats.hitRate() * 100),
                "missRate", String.format("%.2f%%", stats.missRate() * 100),
                "evictionCount", stats.evictionCount(),
                "loadCount", stats.loadCount(),
                "totalLoadTime", stats.totalLoadTime(),
                "averageLoadPenalty", String.format("%.2fms", stats.averageLoadPenalty() / 1_000_000.0)
        );
    }
}