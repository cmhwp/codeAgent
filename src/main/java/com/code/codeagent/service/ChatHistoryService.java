package com.code.codeagent.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.code.codeagent.model.dto.chathistory.ChatHistoryQueryRequest;
import com.code.codeagent.model.entity.ChatHistory;
import com.code.codeagent.model.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层
 *
 * @author CodeAgent
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话历史
     *
     * @param appId       应用 id
     * @param message     消息
     * @param messageType 消息类型
     * @param userId      用户 id
     * @param parentId    父消息 id（可选）
     * @return 新增记录的id
     */
    Long addChatMessage(Long appId, String message, String messageType, Long userId, Long parentId);

    /**
     * 根据应用 id 删除对话历史
     *
     * @param appId 应用id
     * @return 是否成功
     */
    boolean deleteByAppId(Long appId);

    /**
     * 分页查询某 APP 的对话记录
     *
     * @param appId          应用id
     * @param pageSize       页面大小
     * @param lastCreateTime 最后创建时间（游标分页）
     * @param loginUser      登录用户
     * @return 分页结果
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    /**
     * 根据用户消息ID重新生成AI回复
     *
     * @param userMessageId 用户消息ID
     * @param appId         应用ID
     * @param loginUser     登录用户
     * @return AI回复的流式数据
     */
    Object retryGenerate(Long userMessageId, Long appId, User loginUser);

    /**
     * 根据父消息ID获取所有子消息（AI回复）
     *
     * @param parentId 父消息ID
     * @return 子消息列表
     */
    List<ChatHistory> getChildMessages(Long parentId);

    /**
     * 删除用户消息的所有AI回复（用于重新生成前清理）
     *
     * @param parentId 父消息ID（用户消息ID）
     * @return 是否成功
     */
    boolean deleteAiRepliesByParentId(Long parentId);

    /**
     * 获取对话历史的上下文（用于AI对话）
     *
     * @param appId    应用ID
     * @param maxCount 最大条数
     * @return 历史消息列表（按时间正序）
     */
    List<ChatHistory> getChatContext(Long appId, int maxCount);

    /**
     * 构造查询条件
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper<ChatHistory> getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);
}