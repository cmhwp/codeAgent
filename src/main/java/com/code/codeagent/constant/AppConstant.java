package com.code.codeagent.constant;

/**
 * 应用常量
 *
 * @author CodeAgent
 */
public interface AppConstant {

    /**
     * 代码输出根目录
     */
    String CODE_OUTPUT_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 代码部署根目录
     */
    String CODE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_deploy";

    /**
     * 应用部署域名
     */
    String APP_DEPLOY_DOMAIN = "http://localhost";

    /**
     * 精选应用的优先级
     */
    int GOOD_APP_PRIORITY = 1;

    /**
     * 默认应用优先级
     */
    int DEFAULT_APP_PRIORITY = 0;

    /**
     * 默认应用名称最大长度
     */
    int DEFAULT_APP_NAME_MAX_LENGTH = 12;
}