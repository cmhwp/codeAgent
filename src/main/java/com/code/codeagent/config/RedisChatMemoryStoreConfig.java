package com.code.codeagent.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 持久化对话记忆配置
 * 
 * @author CodeAgent
 */
@Configuration
@Slf4j
public class RedisChatMemoryStoreConfig {

    @Value("${langchain4j.redis.host:localhost}")
    private String host;
    
    @Value("${langchain4j.redis.port:6379}")
    private int port;
    
    @Value("${langchain4j.redis.password:}")
    private String password;
    
    @Value("${langchain4j.redis.ttl:3600}")
    private long ttl;

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        log.info("初始化 RedisChatMemoryStore，host: {}, port: {}, ttl: {}s", host, port, ttl);
        
        RedisChatMemoryStore.Builder builder = RedisChatMemoryStore.builder()
                .host(host)
                .port(port)
                .ttl(ttl);
                
        // 如果密码不为空，则设置密码
        if (password != null && !password.trim().isEmpty()) {
            builder.password(password);
        }
        
        return builder.build();
    }
}