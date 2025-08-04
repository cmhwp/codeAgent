package com.code.codeagent.config;

import com.code.codeagent.ai.AiCodeGeneratorService;
import com.code.codeagent.service.ChatHistoryService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

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
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Autowired(required = false)
    private ChatHistoryService chatHistoryService;

    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<Long, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，appId: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 根据 appId 获取缓存的服务实例
     *
     * @param appId 应用ID
     * @return AI代码生成器服务实例
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return serviceCache.get(appId, this::createAiCodeGeneratorService);
    }

    /**
     * 创建新的 AI 服务实例
     *
     * @param appId 应用ID
     * @return AI代码生成器服务实例
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId) {
        log.info("为 appId: {} 创建新的 AI 服务实例", appId);
        
        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)  // 最大保存20条消息
                .build();
        
        // 从数据库中加载对话历史到记忆中（如果ChatHistoryService可用）
        if (chatHistoryService != null) {
            try {
                chatHistoryService.getChatContext(appId, 20);
                log.debug("已为 appId: {} 加载对话历史", appId);
            } catch (Exception e) {
                log.warn("加载对话历史失败，appId: {}, 错误: {}", appId, e.getMessage());
            }
        }
        
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();
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
        
        return getAiCodeGeneratorService(0L);  // 使用默认的 appId = 0
    }

    /**
     * 清除指定应用的缓存
     * 
     * @param appId 应用ID
     */
    public void evictCache(Long appId) {
        serviceCache.invalidate(appId);
        log.info("已清除 appId: {} 的AI服务缓存", appId);
    }

    /**
     * 清除所有缓存
     */
    public void evictAllCache() {
        serviceCache.invalidateAll();
        log.info("已清除所有AI服务缓存");
    }

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息字符串
     */
    public String getCacheStats() {
        return String.format("缓存统计 - 大小: %d, 命中率: %.2f%%, 驱逐次数: %d", 
                serviceCache.estimatedSize(),
                serviceCache.stats().hitRate() * 100,
                serviceCache.stats().evictionCount());
    }
}