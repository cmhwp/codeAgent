package com.code.codeagent;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

/**
 * MyBatis Plus 代码生成器
 */
public class CodeGenerator {

    public static void main(String[] args) {
        // 数据库配置
        String url = "jdbc:mysql://localhost:3306/code_agent?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true";
        String username = "root";
        String password = "123456";

        // 项目路径
        String projectPath = System.getProperty("user.dir");
        String moduleName = "";

        FastAutoGenerator.create(url, username, password)
                // 全局配置
                .globalConfig(builder -> {
                    builder.author("CodeAgent") // 设置作者
                            .enableSwagger() // 开启 swagger 模式
                            .outputDir(projectPath + "/src/main/java") // 指定输出目录
                            .dateType(DateType.TIME_PACK) // 时间策略
                            .commentDate("yyyy-MM-dd"); // 注释日期
                })
                // 包配置
                .packageConfig(builder -> {
                    builder.parent("com.code.codeagent") // 设置父包名
                            .moduleName(moduleName) // 设置父包模块名
                            .entity("model.entity") // 设置实体类包名
                            .mapper("mapper") // 设置 Mapper 接口包名
                            .service("service") // 设置 Service 接口包名
                            .serviceImpl("service.impl") // 设置 Service 实现类包名
                            .controller("controller") // 设置 Controller 包名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, projectPath + "/src/main/resources/mapper")); // 设置mapperXml生成路径
                })
                // 策略配置
                .strategyConfig(builder -> {
                    builder.addInclude("user") // 设置需要生成的表名
                            .addTablePrefix("t_", "c_") // 设置过滤表前缀
                            // Entity 策略配置
                            .entityBuilder()
                            .enableLombok() // 开启 lombok 模式
                            .enableTableFieldAnnotation() // 开启生成实体时生成字段注解
                            .naming(NamingStrategy.underline_to_camel) // 数据库表映射到实体的命名策略
                            .columnNaming(NamingStrategy.underline_to_camel) // 数据库表字段映射到实体的命名策略
                            .enableFileOverride() // 覆盖已生成文件
                            .logicDeleteColumnName("isDelete") // 逻辑删除字段名
                            .enableActiveRecord() // 开启 ActiveRecord 模式
                            // Mapper 策略配置
                            .mapperBuilder()
                            .enableMapperAnnotation() // 开启 @Mapper 注解
                            .enableFileOverride() // 覆盖已生成文件
                            // Service 策略配置
                            .serviceBuilder()
                            .formatServiceFileName("%sService") // 设置 service 接口名称格式
                            .formatServiceImplFileName("%sServiceImpl") // 设置 service 实现类名称格式
                            .enableFileOverride() // 覆盖已生成文件
                            // Controller 策略配置
                            .controllerBuilder()
                            .enableHyphenStyle() // 开启驼峰转连字符
                            .enableRestStyle() // 开启生成@RestController 控制器
                            .enableFileOverride(); // 覆盖已生成文件
                })
                // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();

        System.out.println("代码生成完成！");
    }
} 