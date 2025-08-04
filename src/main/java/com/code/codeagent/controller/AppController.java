package com.code.codeagent.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.code.codeagent.common.BaseResponse;
import com.code.codeagent.common.DeleteRequest;
import com.code.codeagent.common.ResultUtils;
import com.code.codeagent.constant.AppConstant;
import com.code.codeagent.constant.UserConstant;
import com.code.codeagent.exception.BusinessException;
import com.code.codeagent.exception.ErrorCode;
import com.code.codeagent.model.dto.app.*;
import com.code.codeagent.model.entity.App;
import com.code.codeagent.model.entity.User;
import com.code.codeagent.model.enums.CodeGenTypeEnum;
import com.code.codeagent.model.vo.AppVO;
import com.code.codeagent.service.AppService;
import com.code.codeagent.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 应用接口
 *
 * @author CodeAgent
 */
@RestController
@RequestMapping("/app")
@Slf4j
@Tag(name = "appManagement", description = "应用相关接口")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    /**
     * 通过对话生成应用代码（流式响应）
     *
     * @param appId 应用ID
     * @param message 用户消息
     * @return 流式响应
     */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SaCheckLogin
    @Operation(summary = "对话生成代码", description = "通过对话生成应用代码（流式响应）")
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                                       @RequestParam String message) {
        // 参数校验
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID错误");
        }
        if (StrUtil.isBlank(message)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提示词不能为空");
        }
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser();
        
        // 调用服务生成代码（SSE 流式返回）
        Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);
        return contentFlux
                .map(chunk -> {
                    Map<String, String> wrapper = Map.of("d", chunk);
                    String jsonData = JSONUtil.toJsonStr(wrapper);
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)
                            .build();
                })
                .concatWith(Mono.just(
                        // 发送结束事件
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                ));
    }

    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @return 部署URL
     */
    @PostMapping("/deploy")
    @SaCheckLogin
    @Operation(summary = "应用部署", description = "部署应用并返回访问URL")
    public BaseResponse<String> deployApp(@Valid @RequestBody AppDeployRequest appDeployRequest) {
        Long appId = appDeployRequest.getAppId();
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser();
        
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        
        return ResultUtils.success(deployUrl);
    }

    /**
     * 创建应用
     *
     * @param appAddRequest 创建应用请求
     * @return 应用id
     */
    @PostMapping("/add")
    @SaCheckLogin
    @Operation(summary = "创建应用", description = "创建新的应用")
    public BaseResponse<Long> addApp(@Valid @RequestBody AppAddRequest appAddRequest) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser();
        
        // 构造入库对象
        App app = new App();
        BeanUtils.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        
        // 应用名称暂时为 initPrompt 前几位字符
        String initPrompt = appAddRequest.getInitPrompt();
        String appName = initPrompt.substring(0, Math.min(initPrompt.length(), AppConstant.DEFAULT_APP_NAME_MAX_LENGTH));
        app.setAppName(appName);
        
        // 如果没有指定代码生成类型，默认设置为多文件生成
        if (StrUtil.isBlank(app.getCodeGenType())) {
            app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        }
        
        // 设置默认优先级
        app.setPriority(AppConstant.DEFAULT_APP_PRIORITY);
        
        // 参数校验
        appService.validApp(app, true);
        
        // 插入数据库
        boolean result = appService.save(app);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建应用失败");
        }
        
        return ResultUtils.success(app.getId());
    }

    /**
     * 更新应用（用户只能更新自己的应用）
     *
     * @param appUpdateRequest 更新请求
     * @return 更新结果
     */
    @PostMapping("/update")
    @SaCheckLogin
    @Operation(summary = "更新应用", description = "更新应用信息（用户只能更新自己的应用）")
    public BaseResponse<Boolean> updateApp(@Valid @RequestBody AppUpdateRequest appUpdateRequest) {
        Long id = appUpdateRequest.getId();
        User loginUser = userService.getLoginUser();
        
        // 判断是否存在
        App oldApp = appService.getById(id);
        if (oldApp == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        
        // 仅本人可更新
        if (!oldApp.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限更新该应用");
        }
        
        App app = new App();
        BeanUtils.copyProperties(appUpdateRequest, app);
        app.setId(id);
        app.setEditTime(LocalDateTime.now());
        
        // 参数校验
        appService.validApp(app, false);
        
        boolean result = appService.updateById(app);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新应用失败");
        }
        
        return ResultUtils.success(true);
    }

    /**
     * 删除应用（用户只能删除自己的应用）
     *
     * @param deleteRequest 删除请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    @SaCheckLogin
    @Operation(summary = "删除应用", description = "删除应用（用户只能删除自己的应用）")
    public BaseResponse<Boolean> deleteApp(@Valid @RequestBody DeleteRequest deleteRequest) {
        Long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser();
        
        // 判断是否存在
        App oldApp = appService.getById(id);
        if (oldApp == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        
        // 仅本人或管理员可删除
        if (!oldApp.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除该应用");
        }
        
        boolean result = appService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取应用详情
     *
     * @param id 应用id
     * @return 应用详情
     */
    @GetMapping("/get/vo")
    @Operation(summary = "获取应用详情", description = "根据ID获取应用详细信息")
    public BaseResponse<AppVO> getAppVOById(@RequestParam long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID无效");
        }
        
        // 查询数据库
        App app = appService.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        
        // 获取封装类（包含用户信息）
        AppVO appVO = appService.getAppVO(app);
        return ResultUtils.success(appVO);
    }

    /**
     * 分页获取当前用户创建的应用列表
     *
     * @param appQueryRequest 查询请求
     * @return 应用列表
     */
    @PostMapping("/my/list/page/vo")
    @SaCheckLogin
    @Operation(summary = "我的应用列表", description = "分页获取当前用户创建的应用列表")
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(@Valid @RequestBody AppQueryRequest appQueryRequest) {
        User loginUser = userService.getLoginUser();
        
        // 限制每页最多20个
        long pageSize = appQueryRequest.getPageSize();
        if (pageSize > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "每页最多查询20个应用");
        }
        
        long pageNum = appQueryRequest.getPageNum();
        
        // 只查询当前用户的应用
        appQueryRequest.setUserId(loginUser.getId());
        
        Page<App> appPage = appService.page(new Page<>(pageNum, pageSize), 
                appService.getQueryWrapper(appQueryRequest));
        
        // 数据封装
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotal());
        appVOPage.setRecords(appVOList);
        
        return ResultUtils.success(appVOPage);
    }

    /**
     * 分页获取精选应用列表
     *
     * @param appQueryRequest 查询请求
     * @return 精选应用列表
     */
    @PostMapping("/good/list/page/vo")
    @Operation(summary = "精选应用列表", description = "分页获取精选应用列表")
    public BaseResponse<Page<AppVO>> listGoodAppVOByPage(@Valid @RequestBody AppQueryRequest appQueryRequest) {
        // 限制每页最多20个
        long pageSize = appQueryRequest.getPageSize();
        if (pageSize > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "每页最多查询20个应用");
        }
        
        long pageNum = appQueryRequest.getPageNum();
        
        // 只查询精选的应用
        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        
        Page<App> appPage = appService.page(new Page<>(pageNum, pageSize), 
                appService.getQueryWrapper(appQueryRequest));
        
        // 数据封装
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotal());
        appVOPage.setRecords(appVOList);
        
        return ResultUtils.success(appVOPage);
    }

    /**
     * 管理员删除应用
     *
     * @param deleteRequest 删除请求
     * @return 删除结果
     */
    @PostMapping("/admin/delete")
    @SaCheckLogin
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员删除应用", description = "管理员删除指定应用")
    public BaseResponse<Boolean> deleteAppByAdmin(@Valid @RequestBody DeleteRequest deleteRequest) {
        Long id = deleteRequest.getId();
        
        // 判断是否存在
        App oldApp = appService.getById(id);
        if (oldApp == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        
        boolean result = appService.removeById(id);
        return ResultUtils.success(result);
    }

    /**
     * 管理员更新应用
     *
     * @param appAdminUpdateRequest 更新请求
     * @return 更新结果
     */
    @PostMapping("/admin/update")
    @SaCheckLogin
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员更新应用", description = "管理员更新应用信息")
    public BaseResponse<Boolean> updateAppByAdmin(@Valid @RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        Long id = appAdminUpdateRequest.getId();
        
        // 判断是否存在
        App oldApp = appService.getById(id);
        if (oldApp == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        
        App app = new App();
        BeanUtils.copyProperties(appAdminUpdateRequest, app);
        app.setId(id);
        app.setEditTime(LocalDateTime.now());
        
        // 参数校验
        appService.validApp(app, false);
        
        boolean result = appService.updateById(app);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新应用失败");
        }
        
        return ResultUtils.success(true);
    }

    /**
     * 管理员分页获取应用列表
     *
     * @param appQueryRequest 查询请求
     * @return 应用列表
     */
    @PostMapping("/admin/list/page/vo")
    @SaCheckLogin
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员应用列表", description = "管理员分页获取应用列表")
    public BaseResponse<Page<AppVO>> listAppVOByPageByAdmin(@Valid @RequestBody AppQueryRequest appQueryRequest) {
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        
        Page<App> appPage = appService.page(new Page<>(pageNum, pageSize), 
                appService.getQueryWrapper(appQueryRequest));
        
        // 数据封装
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotal());
        appVOPage.setRecords(appVOList);
        
        return ResultUtils.success(appVOPage);
    }

    /**
     * 管理员根据id获取应用详情
     *
     * @param id 应用id
     * @return 应用详情
     */
    @GetMapping("/admin/get/vo")
    @SaCheckLogin
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员获取应用详情", description = "管理员根据ID获取应用详细信息")
    public BaseResponse<AppVO> getAppVOByIdByAdmin(@RequestParam long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID无效");
        }
        
        // 查询数据库
        App app = appService.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        
        // 获取封装类
        AppVO appVO = appService.getAppVO(app);
        return ResultUtils.success(appVO);
    }
}