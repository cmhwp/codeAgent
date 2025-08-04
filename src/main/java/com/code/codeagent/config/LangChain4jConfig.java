package com.code.codeagent.config;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j 配置
 * 禁用不需要的自动配置
 *
 * @author CodeAgent
 */
@Configuration
@EnableAutoConfiguration(exclude = {
        RedisEmbeddingStoreAutoConfiguration.class  // 禁用 RedisEmbeddingStore 自动配置（如果不需要向量存储功能）
})
public class LangChain4jConfig {
    
    // 这个类主要用于排除不需要的自动配置
    // 如果将来需要 RedisEmbeddingStore，可以移除 exclude 中的配置
}