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

### 2. 用户管理系统
- ✅ 用户注册（账号唯一性校验、密码加密）
- ✅ 用户登录（密码验证、状态检查）
- ✅ 用户信息管理（脱敏处理）
- ✅ 个人信息更新（昵称、头像、简介、邮箱）
- ✅ 用户状态管理（正常/禁用）
- ✅ 邮箱唯一性校验

### 3. 跨域支持
- ✅ 全局跨域配置
- ✅ 支持所有HTTP方法
- ✅ 支持携带凭证

### 4. 统一响应格式
- ✅ 统一返回结果封装
- ✅ 全局异常处理
- ✅ Sa-Token异常特殊处理

### 5. 代码生成器
- ✅ MyBatis Plus 代码生成器
- ✅ 自动生成 Entity、Mapper、Service、Controller
- ✅ 支持 Swagger 注解
- ✅ 支持 Lombok

## 快速开始

### 1. 环境要求
- JDK 21+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 2. 数据库初始化

执行 `script/db.sql` 文件创建数据库和表：

```sql
-- 创建数据库
create database if not exists code_agent;

-- 执行完整的SQL脚本
-- 包含用户表的创建
```

### 3. 配置修改

修改 `application.yml` 中的数据库和Redis配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/code_agent
    username: your_username
    password: your_password
  
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
```

### 4. 启动项目

```bash
mvn spring-boot:run
```

访问 API 文档：http://localhost:8123/doc.html

## 代码生成器使用

### 1. 配置代码生成器

编辑 `src/test/java/com/code/codeagent/CodeGenerator.java` 文件：

```java
// 修改数据库连接信息
String url = "jdbc:mysql://localhost:3306/code_agent";
String username = "root";
String password = "123456";

// 指定要生成的表名
builder.addInclude("user", "其他表名")
```

### 2. 运行代码生成器

```bash
# 方式1: 直接运行main方法
# 在IDE中运行 CodeGenerator.main()

# 方式2: 使用Maven命令
mvn compile exec:java -Dexec.mainClass="com.code.codeagent.CodeGenerator" -Dexec.classpathScope="test"
```

### 3. 生成的文件位置

- **Entity**: `src/main/java/com/code/codeagent/model/entity/`
- **Mapper**: `src/main/java/com/code/codeagent/mapper/`
- **Service**: `src/main/java/com/code/codeagent/service/`
- **ServiceImpl**: `src/main/java/com/code/codeagent/service/impl/`
- **Controller**: `src/main/java/com/code/codeagent/controller/`
- **XML**: `src/main/resources/mapper/`

## API 接口

### 用户管理

| 接口 | 方法 | 说明 | 是否需要登录 |
|------|------|------|-------------|
| `/user/register` | POST | 用户注册 | ❌ |
| `/user/login` | POST | 用户登录 | ❌ |
| `/user/logout` | POST | 用户登出 | ❌ |
| `/user/get/login` | GET | 获取当前登录用户 | ✅ |
| `/user/get/vo` | GET | 获取用户信息(脱敏) | ❌ |
| `/user/update/my` | POST | 更新个人信息 | ✅ |

### 认证状态

| 接口 | 方法 | 说明 | 是否需要登录 |
|------|------|------|-------------|
| `/auth/isLogin` | GET | 查询登录状态 | ❌ |
| `/auth/userInfo` | GET | 获取认证用户信息 | ✅ |
| `/auth/tokenInfo` | GET | 获取Token信息 | ✅ |
| `/auth/kickout` | POST | 踢人下线 | ✅ |

### 注册示例

```json
POST /user/register
{
  "userAccount": "testuser",
  "userPassword": "12345678",
  "checkPassword": "12345678",
  "userName": "测试用户",     // 可选
  "userEmail": "test@example.com"  // 可选
}
```

### 登录示例

```json
POST /user/login
{
  "userAccount": "testuser",
  "userPassword": "12345678"
}
```

### 更新个人信息示例

```json
POST /user/update/my
{
  "userName": "新昵称",
  "userAvatar": "https://example.com/avatar.jpg",
  "userProfile": "这是我的个人简介",
  "userEmail": "newemail@example.com"
}
```

## Sa-Token 配置说明

项目已预配置 Sa-Token 相关设置：

- **Token 有效期**：30天
- **Token 风格**：UUID
- **允许并发登录**：是
- **Token 前缀**：`Bearer`
- **Redis 持久化**：已启用
- **密码加盐**：使用 `codeagent` 作为盐值

## 安全特性

### 1. 密码安全
- 使用 MD5 + 盐值加密
- 密码最小长度8位
- 不在响应中返回密码

### 2. 账号安全
- 账号唯一性校验
- 不允许特殊字符
- 账号最小长度4位

### 3. 邮箱安全
- 邮箱格式校验
- 邮箱唯一性校验（注册和更新时）
- 邮箱为可选字段

### 4. 接口安全
- Sa-Token 全局拦截
- 登录状态校验
- 用户状态检查（正常/禁用）
- 参数校验和长度限制

## 字段说明

### 用户注册
- **必填**：账号、密码、确认密码
- **可选**：昵称、邮箱

### 用户登录
- **必填**：账号、密码

### 个人信息更新
- **全部可选**：昵称、头像、简介、邮箱
- **自动验证**：邮箱格式、字段长度、邮箱唯一性

## 开发工具

### 1. 代码生成器
- MyBatis Plus 自动生成代码
- 支持自定义模板
- 一键生成完整的 CRUD 代码

### 2. API 文档
- Knife4j 美观的文档界面
- 支持在线测试
- Swagger 注解支持

### 3. 工具类库
- HuTool 丰富的工具方法
- 字符串、集合、加密等常用功能

## 项目结构

```
src/main/java/com/code/codeagent/
├── CodeAgentApplication.java      # 启动类
├── common/                        # 通用类
│   ├── BaseResponse.java         # 统一响应格式
│   ├── ResultUtils.java          # 结果工具类
│   └── UserConstant.java         # 用户常量
├── config/                       # 配置类
│   ├── SaTokenConfig.java        # Sa-Token配置
│   ├── MyBatisPlusConfig.java    # MyBatis Plus配置
│   └── MetaObjectHandlerConfig.java # 字段自动填充
├── controller/                   # 控制器
│   ├── UserController.java       # 用户控制器
│   └── AuthController.java       # 认证控制器
├── exception/                    # 异常处理
│   ├── GlobalExceptionHandler.java # 全局异常处理器
│   ├── BusinessException.java    # 业务异常
│   └── ErrorCode.java           # 错误码枚举
├── mapper/                       # 数据访问层
│   └── UserMapper.java          # 用户Mapper
├── model/                        # 数据模型
│   ├── entity/                   # 实体类
│   │   └── User.java
│   ├── dto/                      # 数据传输对象
│   │   ├── UserLoginRequest.java
│   │   ├── UserRegisterRequest.java
│   │   └── UserUpdateMyRequest.java
│   ├── vo/                       # 视图对象
│   │   ├── UserVO.java
│   │   └── LoginUserVO.java
│   └── enums/                    # 枚举类
│       ├── UserRoleEnum.java
│       └── UserStatusEnum.java
└── service/                      # 服务层
    ├── UserService.java          # 用户服务接口
    └── impl/
        └── UserServiceImpl.java  # 用户服务实现
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
```

### 其他重要依赖
- **MyBatis Plus**：简化数据库操作
- **Redis**：缓存和Session存储
- **Knife4j**：API文档生成
- **HuTool**：Java工具类
- **FastJSON2**：JSON处理
- **Lombok**：简化代码

## 注意事项

1. **数据库**：确保数据库已创建并执行了建表SQL
2. **Redis**：Sa-Token 使用 Redis 存储 Token 信息，确保 Redis 服务正常运行
3. **跨域**：已全局配置跨域支持，前端可直接调用
4. **权限控制**：可通过 `@SaCheckLogin`、`@SaCheckRole`、`@SaCheckPermission` 注解进行权限控制
5. **密码安全**：生产环境建议使用更强的加密算法（如BCrypt）
6. **字段验证**：所有用户输入都经过严格的格式和长度验证
7. **邮箱唯一性**：注册和更新个人信息时都会检查邮箱是否已被使用

## 开发参考

- [Sa-Token 官方文档](https://sa-token.cc/)
- [MyBatis Plus 官方文档](https://baomidou.com/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Knife4j 文档](https://doc.xiaominfo.com/)

## License

MIT License 