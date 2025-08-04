package com.code.codeagent.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.code.codeagent.constant.UserConstant;
import com.code.codeagent.exception.BusinessException;
import com.code.codeagent.exception.ErrorCode;
import com.code.codeagent.exception.ThrowUtils;
import com.code.codeagent.mapper.ChatHistoryMapper;
import com.code.codeagent.model.dto.chathistory.ChatHistoryQueryRequest;
import com.code.codeagent.model.entity.App;
import com.code.codeagent.model.entity.ChatHistory;
import com.code.codeagent.model.entity.User;
import com.code.codeagent.model.enums.ChatHistoryMessageTypeEnum;
import com.code.codeagent.service.AppService;
import com.code.codeagent.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现
 *
 * @author CodeAgent
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    @Lazy
    private AppService appService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addChatMessage(Long appId, String message, String messageType, Long userId, Long parentId) {
        // 基础校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        
        // 验证消息类型是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型");
        
        // 如果是AI消息，必须有父消息ID
        if (ChatHistoryMessageTypeEnum.AI.getValue().equals(messageType)) {
            ThrowUtils.throwIf(parentId == null || parentId <= 0, ErrorCode.PARAMS_ERROR, "AI消息必须关联用户消息");
            // 验证父消息是否存在且为用户消息
            ChatHistory parentMessage = this.getById(parentId);
            ThrowUtils.throwIf(parentMessage == null, ErrorCode.PARAMS_ERROR, "父消息不存在");
            ThrowUtils.throwIf(!ChatHistoryMessageTypeEnum.USER.getValue().equals(parentMessage.getMessageType()), 
                    ErrorCode.PARAMS_ERROR, "AI消息只能回复用户消息");
        }
        
        // 插入数据库
        ChatHistory chatHistory = new ChatHistory()
                .setAppId(appId)
                .setMessage(message)
                .setMessageType(messageType)
                .setUserId(userId)
                .setParentId(parentId);
        
        boolean success = this.save(chatHistory);
        ThrowUtils.throwIf(!success, ErrorCode.SYSTEM_ERROR, "保存对话历史失败");
        
        return chatHistory.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        QueryWrapper<ChatHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("appId", appId);
        return this.remove(queryWrapper);
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper<ChatHistory> queryWrapper = this.getQueryWrapper(queryRequest);
        
        // 查询数据
        return this.page(new Page<>(1, pageSize), queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object retryGenerate(Long userMessageId, Long appId, User loginUser) {
        ThrowUtils.throwIf(userMessageId == null || userMessageId <= 0, ErrorCode.PARAMS_ERROR, "用户消息ID不能为空");
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        // 验证用户消息是否存在
        ChatHistory userMessage = this.getById(userMessageId);
        ThrowUtils.throwIf(userMessage == null, ErrorCode.NOT_FOUND_ERROR, "用户消息不存在");
        ThrowUtils.throwIf(!ChatHistoryMessageTypeEnum.USER.getValue().equals(userMessage.getMessageType()), 
                ErrorCode.PARAMS_ERROR, "只能重试用户消息");
        ThrowUtils.throwIf(!userMessage.getAppId().equals(appId), ErrorCode.PARAMS_ERROR, "消息与应用不匹配");
        
        // 验证权限
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权限操作该应用");
        
        // 删除该用户消息的所有AI回复
        this.deleteAiRepliesByParentId(userMessageId);
        
        // 调用AppService重新生成代码（这里返回流式数据）
        // 注意：这里需要与AppService的chatToGenCode方法集成
        return appService.retryGenerateCode(appId, userMessage.getMessage(), loginUser, userMessageId);
    }

    @Override
    public List<ChatHistory> getChildMessages(Long parentId) {
        ThrowUtils.throwIf(parentId == null || parentId <= 0, ErrorCode.PARAMS_ERROR, "父消息ID不能为空");
        
        QueryWrapper<ChatHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parentId", parentId)
                .orderBy(true, true, "createTime");
        return this.list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAiRepliesByParentId(Long parentId) {
        ThrowUtils.throwIf(parentId == null || parentId <= 0, ErrorCode.PARAMS_ERROR, "父消息ID不能为空");
        
        QueryWrapper<ChatHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parentId", parentId)
                .eq("messageType", ChatHistoryMessageTypeEnum.AI.getValue());
        return this.remove(queryWrapper);
    }

    @Override
    public List<ChatHistory> getChatContext(Long appId, int maxCount) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        
        try {
            QueryWrapper<ChatHistory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("appId", appId)
                    .orderBy(true, false, "createTime")
                    .last("LIMIT " + maxCount);
            
            List<ChatHistory> historyList = this.list(queryWrapper);
            if (historyList.isEmpty()) {
                return historyList;
            }
            
            // 反转列表，确保按照时间正序（老的在前，新的在后）
            return historyList.stream()
                    .sorted((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()))
                    .toList();
        } catch (Exception e) {
            log.error("获取对话上下文失败，appId: {}, error: {}", appId, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public QueryWrapper<ChatHistory> getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper<ChatHistory> queryWrapper = new QueryWrapper<>();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        Long parentId = chatHistoryQueryRequest.getParentId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        
        // 拼接查询条件
        queryWrapper.eq(id != null, "id", id)
                .like(StrUtil.isNotBlank(message), "message", message)
                .eq(StrUtil.isNotBlank(messageType), "messageType", messageType)
                .eq(appId != null, "appId", appId)
                .eq(userId != null, "userId", userId)
                .eq(parentId != null, "parentId", parentId);
        
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(true, "ascend".equals(sortOrder), sortField);
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy(true, false, "createTime");
        }
        
        return queryWrapper;
    }
}