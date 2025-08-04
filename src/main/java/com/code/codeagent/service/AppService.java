package com.code.codeagent.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.code.codeagent.model.dto.app.AppQueryRequest;
import com.code.codeagent.model.entity.App;
import com.code.codeagent.model.entity.User;
import com.code.codeagent.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用服务接口
 *
 * @author CodeAgent
 * @since 2024-12-19
 */
public interface AppService extends IService<App> {

    /**
     * 通过对话生成应用代码（流式响应）
     *
     * @param appId 应用ID
     * @param message 用户消息
     * @param loginUser 登录用户
     * @return 流式响应
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 重新生成代码（基于历史消息重试）
     *
     * @param appId 应用ID
     * @param message 用户消息
     * @param loginUser 登录用户
     * @param parentMessageId 父消息ID（用户消息ID）
     * @return 流式响应
     */
    Flux<String> retryGenerateCode(Long appId, String message, User loginUser, Long parentMessageId);

    /**
     * 应用部署
     *
     * @param appId 应用ID
     * @param loginUser 登录用户
     * @return 部署URL
     */
    String deployApp(Long appId, User loginUser);

    /**
     * 获取应用封装类
     *
     * @param app 应用实体
     * @return 应用VO
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用封装类列表
     *
     * @param appList 应用实体列表
     * @return 应用VO列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 构造应用查询条件
     *
     * @param appQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 校验应用参数
     *
     * @param app 应用实体
     * @param add 是否为创建校验
     */
    void validApp(App app, boolean add);
}