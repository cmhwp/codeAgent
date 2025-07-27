# CodeAgent 项目

基于 Spring Boot 3.3.5 的后端项目，集成了 Sa-Token 权限认证框架和其他便于开发的库。

## 技术栈

- **Java 21**
- **Spring Boot 3.3.5**
- **Sa-Token 1.44.0** - 权限认证框架
- **MyBatis Plus 3.5.7** - ORM框架
- **Redis** - 缓存和Session存储
- **MySQL** - 数据库
- **Knife4j** - API文档工具
- **HuTool** - Java工具类库
- **Lombok** - 简化Java代码

## 主要功能

### 1. Sa-Token 权限认证
- ✅ 用户登录/登出
- ✅ Token管理（JWT风格）
- ✅ 权限校验
- ✅ 角色管理
- ✅ 踢人下线
- ✅ Session管理

### 2. 跨域支持
- ✅ 全局跨域配置
- ✅ 支持所有HTTP方法
- ✅ 支持携带凭证

### 3. 统一响应格式
- ✅ 统一返回结果封装
- ✅ 全局异常处理
- ✅ Sa-Token异常特殊处理

## 快速开始

### 1. 环境要求
- JDK 21+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 2. 配置修改

修改 `application.yml` 中的数据库和Redis配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database
    username: your_username
    password: your_password
  
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
```

### 3. 启动项目

```bash
mvn spring-boot:run
```

访问 API 文档：http://localhost:8123/doc.html

## API 接口

### 认证相关

| 接口 | 方法 | 说明 | 是否需要登录 |
|------|------|------|-------------|
| `/auth/login` | POST | 用户登录 | ❌ |
| `/auth/logout` | POST | 用户登出 | ❌ |
| `/auth/isLogin` | GET | 查询登录状态 | ❌ |
| `/auth/userInfo` | GET | 获取用户信息 | ✅ |
| `/auth/tokenInfo` | GET | 获取Token信息 | ✅ |
| `/auth/kickout` | POST | 踢人下线 | ✅ |

### 测试账号
- 用户名：`admin`
- 密码：`123456`

## Sa-Token 配置说明

项目已预配置 Sa-Token 相关设置：

- **Token 有效期**：30天
- **Token 风格**：UUID
- **允许并发登录**：是
- **Token 前缀**：`Bearer`
- **Redis 持久化**：已启用

## 开发工具

### 1. 代码生成器

项目集成了 MyBatis Plus 代码生成器，可快速生成 Entity、Mapper、Service、Controller 等代码。

### 2. API 文档

使用 Knife4j 提供美观的 API 文档界面，支持在线测试。

### 3. 工具类库

集成 HuTool 工具类库，提供丰富的工具方法。

## 项目结构

```
src/main/java/com/code/codeagent/
├── CodeAgentApplication.java      # 启动类
├── common/                        # 通用类
│   ├── BaseResponse.java         # 统一响应格式
│   ├── ResultUtils.java          # 结果工具类
│   └── ...
├── config/                       # 配置类
│   ├── SaTokenConfig.java        # Sa-Token配置
│   └── MyBatisPlusConfig.java    # MyBatis Plus配置
├── controller/                   # 控制器
│   ├── AuthController.java       # 认证控制器
│   └── ExampleController.java    # 示例控制器
├── exception/                    # 异常处理
│   ├── GlobalExceptionHandler.java # 全局异常处理器
│   └── ...
└── service/                      # 服务层
    └── impl/
```

## 依赖说明

### Sa-Token 相关
```xml
<!-- Sa-Token 核心 -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-spring-boot3-starter</artifactId>
    <version>1.44.0</version>
</dependency>

<!-- Sa-Token Redis支持 -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-redis-jackson</artifactId>
    <version>1.44.0</version>
</dependency>

<!-- Sa-Token SSO支持 -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-sso</artifactId>
    <version>1.44.0</version>
</dependency>
```

### 其他重要依赖
- **MyBatis Plus**：简化数据库操作
- **Redis**：缓存和Session存储
- **Knife4j**：API文档生成
- **HuTool**：Java工具类
- **FastJSON2**：JSON处理
- **Lombok**：简化代码

## 注意事项

1. **数据库**：需要手动创建数据库，表结构会通过 MyBatis Plus 自动维护
2. **Redis**：Sa-Token 使用 Redis 存储 Token 信息，确保 Redis 服务正常运行
3. **跨域**：已全局配置跨域支持，前端可直接调用
4. **权限控制**：可通过 `@SaCheckLogin`、`@SaCheckRole`、`@SaCheckPermission` 注解进行权限控制

## 开发参考

- [Sa-Token 官方文档](https://sa-token.cc/)
- [MyBatis Plus 官方文档](https://baomidou.com/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)

## License

MIT License 