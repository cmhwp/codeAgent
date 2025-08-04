package com.code.codeagent.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.code.codeagent.common.BaseResponse;
import com.code.codeagent.common.ResultUtils;
import com.code.codeagent.constant.UserConstant;
import com.code.codeagent.exception.BusinessException;
import com.code.codeagent.exception.ErrorCode;
import com.code.codeagent.exception.ThrowUtils;
import com.code.codeagent.model.dto.chathistory.ChatHistoryAddRequest;
import com.code.codeagent.model.dto.chathistory.ChatHistoryQueryRequest;
import com.code.codeagent.model.dto.chathistory.ChatHistoryRetryRequest;
import com.code.codeagent.model.entity.ChatHistory;
import com.code.codeagent.model.entity.User;
import com.code.codeagent.service.ChatHistoryService;
import com.code.codeagent.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 控制层
 *
 * @author CodeAgent
 */
@RestController
@RequestMapping("/chatHistory")
@Slf4j
@Tag(name = "ChatHistoryController", description = "对话历史控制器")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 添加对话历史
     */
    @PostMapping("/add")
    @Operation(summary = "添加对话历史", description = "添加一条对话历史记录")
    @SaCheckLogin
    public BaseResponse<Long> addChatHistory(@RequestBody @Valid ChatHistoryAddRequest chatHistoryAddRequest) {
        ThrowUtils.throwIf(chatHistoryAddRequest == null, ErrorCode.PARAMS_ERROR);
        
        User loginUser = userService.getLoginUser();
        Long messageId = chatHistoryService.addChatMessage(
                chatHistoryAddRequest.getAppId(),
                chatHistoryAddRequest.getMessage(),
                chatHistoryAddRequest.getMessageType(),
                loginUser.getId(),
                chatHistoryAddRequest.getParentId()
        );
        
        return ResultUtils.success(messageId);
    }

    /**
     * 分页获取对话历史列表
     */
    @PostMapping("/list/page")
    @Operation(summary = "分页获取对话历史", description = "分页查询指定应用的对话历史")
    @SaCheckLogin
    public BaseResponse<Page<ChatHistory>> listChatHistoryByPage(
            @RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        
        long current = chatHistoryQueryRequest.getPageNum();
        long size = chatHistoryQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR, "页面大小不能超过50");
        
        User loginUser = userService.getLoginUser();
        Page<ChatHistory> chatHistoryPage = chatHistoryService.listAppChatHistoryByPage(
                chatHistoryQueryRequest.getAppId(),
                (int) size,
                chatHistoryQueryRequest.getLastCreateTime(),
                loginUser
        );
        
        return ResultUtils.success(chatHistoryPage);
    }

    /**
     * 重新生成AI回复
     */
    @PostMapping(value = "/retry", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "重新生成AI回复", description = "根据用户消息重新生成AI回复（流式返回）")
    @SaCheckLogin
    public Flux<String> retryGenerate(@RequestBody @Valid ChatHistoryRetryRequest retryRequest) {
        ThrowUtils.throwIf(retryRequest == null, ErrorCode.PARAMS_ERROR);
        
        User loginUser = userService.getLoginUser();
        Object result = chatHistoryService.retryGenerate(
                retryRequest.getUserMessageId(),
                retryRequest.getAppId(),
                loginUser
        );
        
        // 假设AppService返回的是Flux<String>
        if (result instanceof Flux) {
            return (Flux<String>) result;
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "重试生成失败");
        }
    }

    /**
     * 获取消息的子回复
     */
    @GetMapping("/children/{parentId}")
    @Operation(summary = "获取子消息", description = "获取指定消息的所有子回复")
    @SaCheckLogin
    public BaseResponse<List<ChatHistory>> getChildMessages(
            @Parameter(description = "父消息ID") @PathVariable Long parentId) {
        
        ThrowUtils.throwIf(parentId == null || parentId <= 0, ErrorCode.PARAMS_ERROR, "父消息ID不能为空");
        
        // 简单权限验证：登录用户即可查看
        userService.getLoginUser();
        
        List<ChatHistory> childMessages = chatHistoryService.getChildMessages(parentId);
        return ResultUtils.success(childMessages);
    }

    /**
     * 删除应用的所有对话历史（管理员功能）
     */
    @DeleteMapping("/deleteByApp/{appId}")
    @SaCheckLogin
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    @Operation(summary = "删除应用对话历史", description = "删除指定应用的所有对话历史（管理员功能）")
    public BaseResponse<Boolean> deleteByAppId(
            @Parameter(description = "应用ID") @PathVariable Long appId) {
        
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        
        boolean result = chatHistoryService.deleteByAppId(appId);
        return ResultUtils.success(result);
    }

    /**
     * 删除指定父消息的AI回复
     */
    @DeleteMapping("/deleteAiReplies/{parentId}")
    @Operation(summary = "删除AI回复", description = "删除指定用户消息的所有AI回复")
    @SaCheckLogin
    public BaseResponse<Boolean> deleteAiReplies(
            @Parameter(description = "父消息ID") @PathVariable Long parentId) {
        
        ThrowUtils.throwIf(parentId == null || parentId <= 0, ErrorCode.PARAMS_ERROR, "父消息ID不能为空");
        
        User loginUser = userService.getLoginUser();
        
        // 验证父消息的权限（简化处理，可以进一步完善）
        ChatHistory parentMessage = chatHistoryService.getById(parentId);
        ThrowUtils.throwIf(parentMessage == null, ErrorCode.NOT_FOUND_ERROR, "父消息不存在");
        
        // 这里可以添加更详细的权限验证逻辑
        
        boolean result = chatHistoryService.deleteAiRepliesByParentId(parentId);
        return ResultUtils.success(result);
    }

    /**
     * 获取对话上下文
     */
    @GetMapping("/context/{appId}")
    @Operation(summary = "获取对话上下文", description = "获取指定应用的对话上下文")
    @SaCheckLogin
    public BaseResponse<List<ChatHistory>> getChatContext(
            @Parameter(description = "应用ID") @PathVariable Long appId,
            @Parameter(description = "最大条数") @RequestParam(defaultValue = "20") int maxCount) {
        
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(maxCount <= 0 || maxCount > 100, ErrorCode.PARAMS_ERROR, "最大条数必须在1-100之间");
        
        // 简单权限验证：登录用户即可查看
        userService.getLoginUser();
        
        List<ChatHistory> context = chatHistoryService.getChatContext(appId, maxCount);
        return ResultUtils.success(context);
    }
}