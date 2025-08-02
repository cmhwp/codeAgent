package com.code.codeagent.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.code.codeagent.constant.AppConstant;
import com.code.codeagent.core.AiCodeGeneratorFacade;
import com.code.codeagent.exception.BusinessException;
import com.code.codeagent.exception.ErrorCode;
import com.code.codeagent.mapper.AppMapper;
import com.code.codeagent.model.dto.app.AppQueryRequest;
import com.code.codeagent.model.entity.App;
import com.code.codeagent.model.entity.User;
import com.code.codeagent.model.enums.CodeGenTypeEnum;
import com.code.codeagent.model.vo.AppVO;
import com.code.codeagent.model.vo.UserVO;
import com.code.codeagent.service.AppService;
import com.code.codeagent.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用服务实现类
 *
 * @author CodeAgent
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID错误");
        }
        if (StrUtil.isBlank(message)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提示词不能为空");
        }
        
        // 2. 查询应用信息
        App app = this.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        
        // 3. 权限校验，仅本人可以和自己的应用对话
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        
        // 4. 获取应用的代码生成类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用代码生成类型错误");
        }
        
        // 5. 调用 AI 生成代码
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        if (appId == null || appId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID错误");
        }
        
        // 2. 查询应用信息
        App app = this.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        
        // 3. 权限校验，仅本人可以部署自己的应用
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        
        // 4. 生成部署key（如果没有的话）
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(8);
            app.setDeployKey(deployKey);
            app.setDeployedTime(LocalDateTime.now());
            this.updateById(app);
        }
        
        // 5. 返回部署URL（这里简单返回一个示例URL）
        return String.format("/deploy/%s", deployKey);
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtils.copyProperties(app, appVO);
        
        // 填充用户信息
        Long userId = app.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(userService.getSafetyUser(user), userVO);
                appVO.setUser(userVO);
            }
        }
        
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        
        // 关联查询用户信息
        Set<Long> userIdSet = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet)
                .stream()
                .collect(Collectors.groupingBy(User::getId));
        
        // 填充信息
        return appList.stream().map(app -> {
            AppVO appVO = new AppVO();
            BeanUtils.copyProperties(app, appVO);
            
            Long userId = app.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(userService.getSafetyUser(user), userVO);
                appVO.setUser(userVO);
            }
            
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest) {
        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        if (appQueryRequest == null) {
            return queryWrapper;
        }
        
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String searchText = appQueryRequest.getSearchText();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        
        // 拼接查询条件
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.like(StrUtil.isNotBlank(appName), "appName", appName);
        queryWrapper.like(StrUtil.isNotBlank(initPrompt), "initPrompt", initPrompt);
        queryWrapper.eq(StrUtil.isNotBlank(codeGenType), "codeGenType", codeGenType);
        queryWrapper.eq(priority != null, "priority", priority);
        queryWrapper.eq(userId != null, "userId", userId);
        
        // 搜索文本
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("appName", searchText)
                    .or().like("initPrompt", searchText));
        }
        
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            boolean isAsc = "ascend".equals(sortOrder);
            queryWrapper.orderBy(true, isAsc, sortField);
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderByDesc("createTime");
        }
        
        return queryWrapper;
    }

    @Override
    public void validApp(App app, boolean add) {
        if (app == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用信息为空");
        }
        
        String appName = app.getAppName();
        String initPrompt = app.getInitPrompt();
        String codeGenType = app.getCodeGenType();
        
        // 创建时，参数不能为空
        if (add) {
            if (StrUtil.isBlank(initPrompt)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "初始化prompt不能为空");
            }
        }
        
        // 有参数则校验
        if (StrUtil.isNotBlank(appName) && appName.length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用名称过长");
        }
        
        if (StrUtil.isNotBlank(initPrompt) && initPrompt.length() > 2000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "初始化prompt过长");
        }
        
        if (StrUtil.isNotBlank(codeGenType)) {
            CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
            if (codeGenTypeEnum == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型错误");
            }
        }
    }
}