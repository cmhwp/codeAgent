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
import com.code.codeagent.exception.ThrowUtils;
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

import cn.hutool.core.io.FileUtil;

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
        ThrowUtils.throwIfNotPositive(appId, ErrorCode.PARAMS_ERROR, "应用ID错误");
        ThrowUtils.throwIfBlank(message, ErrorCode.PARAMS_ERROR, "提示词不能为空");
        ThrowUtils.throwIfNull(loginUser, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIfNull(app, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        
        // 3. 权限校验，仅本人可以和自己的应用对话
        ThrowUtils.throwIfNotEquals(app.getUserId(), loginUser.getId(), ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        
        // 4. 获取应用的代码生成类型
        String codeGenType = app.getCodeGenType();
        ThrowUtils.throwIfBlank(codeGenType, ErrorCode.PARAMS_ERROR, "应用代码生成类型为空");
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        ThrowUtils.throwIfNull(codeGenTypeEnum, ErrorCode.PARAMS_ERROR, "应用代码生成类型错误");
        
        // 5. 调用 AI 生成代码
        log.info("开始为应用生成代码，应用ID：{}，用户ID：{}，消息长度：{}", appId, loginUser.getId(), message.length());
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIfNotPositive(appId, ErrorCode.PARAMS_ERROR, "应用 ID 错误");
        ThrowUtils.throwIfNull(loginUser, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIfNull(app, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        
        // 3. 权限校验，仅本人可以部署自己的应用
        ThrowUtils.throwIfNotEquals(app.getUserId(), loginUser.getId(), ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        
        // 4. 检查代码生成类型是否有效
        String codeGenType = app.getCodeGenType();
        ThrowUtils.throwIfBlank(codeGenType, ErrorCode.PARAMS_ERROR, "应用代码生成类型错误");
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        ThrowUtils.throwIfNull(codeGenTypeEnum, ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
        
        // 5. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 如果没有，则生成 6 位 deployKey（字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
            // 确保生成的 deployKey 唯一
            while (isDeployKeyExists(deployKey)) {
                deployKey = RandomUtil.randomString(6);
            }
        }
        
        // 6. 获取原始代码生成路径（应用访问目录）
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        
        // 7. 检查路径是否存在并有内容
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(), 
                          ErrorCode.SYSTEM_ERROR, "应用代码路径不存在，请先生成应用");
        
        // 检查目录是否为空
        String[] files = sourceDir.list();
        ThrowUtils.throwIf(files == null || files.length == 0, 
                          ErrorCode.SYSTEM_ERROR, "应用代码目录为空，请先生成应用");
        
        // 8. 创建部署目录并复制文件
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        File deployDir = new File(deployDirPath);
        
        try {
            // 确保部署根目录存在
            FileUtil.mkdir(AppConstant.CODE_DEPLOY_ROOT_DIR);
            
            // 如果部署目录已存在，先清空
            if (deployDir.exists()) {
                FileUtil.del(deployDir);
            }
            
            // 复制文件到部署目录
            FileUtil.copyContent(sourceDir, deployDir, true);
            
            log.info("应用部署成功，应用ID：{}，部署路径：{}", appId, deployDirPath);
        } catch (Exception e) {
            log.error("应用部署失败，应用ID：{}，错误信息：{}", appId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用部署失败：" + e.getMessage());
        }
        
        // 9. 更新数据库
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        
        // 10. 返回可访问的 URL 地址
        String deployUrl = String.format("%s/%s", AppConstant.APP_DEPLOY_DOMAIN, deployKey);
        log.info("应用部署成功，应用ID：{}，访问地址：{}", appId, deployUrl);
        return deployUrl;
    }
    
    /**
     * 检查 deployKey 是否已存在
     * 
     * @param deployKey 部署密钥
     * @return 是否存在
     */
    private boolean isDeployKeyExists(String deployKey) {
        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deployKey", deployKey);
        return this.count(queryWrapper) > 0;
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
        
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIdSet = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        
        Map<Long, User> userIdUserMap = userService.listByIds(userIdSet)
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        
        // 填充信息
        return appList.stream().map(app -> {
            AppVO appVO = new AppVO();
            BeanUtils.copyProperties(app, appVO);
            
            Long userId = app.getUserId();
            User user = userIdUserMap.get(userId);
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
        ThrowUtils.throwIfNull(app, ErrorCode.PARAMS_ERROR, "应用信息为空");
        
        String appName = app.getAppName();
        String initPrompt = app.getInitPrompt();
        String codeGenType = app.getCodeGenType();
        
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIfBlank(appName, ErrorCode.PARAMS_ERROR, "应用名称不能为空");
            ThrowUtils.throwIfBlank(initPrompt, ErrorCode.PARAMS_ERROR, "初始化prompt不能为空");
            ThrowUtils.throwIfBlank(codeGenType, ErrorCode.PARAMS_ERROR, "代码生成类型不能为空");
        }
        
        // 有参数则校验
        if (StrUtil.isNotBlank(appName)) {
            ThrowUtils.throwIf(appName.length() > AppConstant.DEFAULT_APP_NAME_MAX_LENGTH * 8, 
                              ErrorCode.PARAMS_ERROR, "应用名称过长，最多允许" + (AppConstant.DEFAULT_APP_NAME_MAX_LENGTH * 8) + "个字符");
        }
        
        if (StrUtil.isNotBlank(initPrompt)) {
            ThrowUtils.throwIf(initPrompt.length() > 2000, 
                              ErrorCode.PARAMS_ERROR, "初始化prompt过长，最多允许2000个字符");
        }
        
        if (StrUtil.isNotBlank(codeGenType)) {
            CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
            ThrowUtils.throwIfNull(codeGenTypeEnum, ErrorCode.PARAMS_ERROR, "代码生成类型错误");
        }
    }
}