# CodeAgent 项目

基于 Spring Boot 3.3.5 的后端项目，集成了 Sa-Token 权限认证框架、邮箱验证码系统和完整的用户管理功能。

## 技术栈

- **Java 21**
- **Spring Boot 3.3.5**
- **Sa-Token 1.44.0** - 权限认证框架
- **MyBatis Plus 3.5.7** - ORM框架
- **Redis** - 缓存和Session存储
- **MySQL** - 数据库
- **Spring Mail** - 邮件发送服务
- **Knife4j** - API文档工具
- **HuTool** - Java工具类库
- **Lombok** - 简化Java代码

## 主要功能

### 1. Sa-Token 权限认证
- ✅ 用户登录/登出
- ✅ Token管理（UUID风格）
- ✅ 权限校验
- ✅ 角色管理
- ✅ 踢人下线
- ✅ Session管理

### 2. 邮箱验证码系统
- ✅ 邮箱验证码发送（美观的HTML模板）
- ✅ 验证码校验和防刷机制
- ✅ 多种用途支持（绑定邮箱、重置密码等）
- ✅ Redis缓存管理
- ✅ 发送频率限制和每日限额

### 3. 用户管理系统
- ✅ 用户注册（账号唯一性校验、密码加密）
- ✅ 用户登录（密码验证、状态检查）
- ✅ 邮箱绑定（验证码验证）
- ✅ 修改密码（验证旧密码）
- ✅ 重置密码（邮箱验证码）
- ✅ 个人信息更新（昵称、头像、简介、邮箱）
- ✅ 用户状态管理（正常/禁用）
- ✅ 邮箱唯一性校验

### 4. 安全特性
- ✅ 密码MD5+盐值加密
- ✅ 邮箱格式和唯一性验证
- ✅ 验证码防刷保护
- ✅ Token自动过期管理
- ✅ 全局异常处理

### 5. 开发工具
- ✅ MyBatis Plus 代码生成器
- ✅ 自动生成 Entity、Mapper、Service、Controller
- ✅ Knife4j API文档
- ✅ 跨域配置
- ✅ 统一响应格式

## 快速开始

### 1. 环境要求
- JDK 21+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+
- 邮箱服务（已配置163邮箱）

### 2. 数据库初始化

执行 `script/db.sql` 文件创建数据库和表：

```sql
-- 创建数据库
create database if not exists code_agent;

-- 执行完整的SQL脚本
-- 包含用户表的创建
```

### 3. 配置修改

修改 `application.yml` 中的数据库、Redis和邮件配置：

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
  
  mail:
    username: your_email@163.com
    password: your_email_auth_code
```

### 4. 启动项目

```bash
mvn spring-boot:run
```

访问 API 文档：http://localhost:8123/doc.html

## Sa-Token 使用说明

### Token 配置参数
- **Token名称**: `satoken`
- **Token前缀**: `Bearer`
- **有效期**: 30天
- **并发登录**: 允许

### 正确的请求方式

#### ✅ 方式一：使用satoken header（推荐）
```bash
curl -X GET \
  -H "satoken: Bearer YOUR_TOKEN_HERE" \
  "http://localhost:8123/user/get/login"
```

#### ✅ 方式二：使用Authorization header
```bash
curl -X GET \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  "http://localhost:8123/user/get/login"
```

#### ❌ 错误方式
```bash
# 错误：使用了错误的header名称
curl -X GET \
  -H "Token: YOUR_TOKEN_HERE" \
  "http://localhost:8123/user/get/login"
```

## API 接口文档

### 用户管理

| 接口 | 方法 | 说明 | 是否需要登录 |
|------|------|------|-------------|
| `/user/register` | POST | 用户注册 | ❌ |
| `/user/login` | POST | 用户登录 | ❌ |
| `/user/logout` | POST | 用户登出 | ❌ |
| `/user/get/login` | GET | 获取当前登录用户 | ✅ |
| `/user/get/vo` | GET | 获取用户信息(脱敏) | ❌ |
| `/user/update/my` | POST | 更新个人信息 | ✅ |

### 邮箱验证码

| 接口 | 方法 | 说明 | 是否需要登录 |
|------|------|------|-------------|
| `/user/send-code` | POST | 发送验证码 | ❌ |
| `/user/bind-email` | POST | 绑定邮箱 | ✅ |

### 密码管理

| 接口 | 方法 | 说明 | 是否需要登录 |
|------|------|------|-------------|
| `/user/change-password` | POST | 修改密码 | ✅ |
| `/user/reset-password` | POST | 重置密码 | ❌ |

### 认证状态

| 接口 | 方法 | 说明 | 是否需要登录 |
|------|------|------|-------------|
| `/auth/isLogin` | GET | 查询登录状态 | ❌ |
| `/auth/userInfo` | GET | 获取认证用户信息 | ✅ |
| `/auth/tokenInfo` | GET | 获取Token信息 | ✅ |
| `/auth/kickout` | POST | 踢人下线 | ✅ |

## 使用示例

### 1. 用户注册
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  "http://localhost:8123/user/register" \
  -d '{
    "userAccount": "testuser",
    "userPassword": "12345678",
    "checkPassword": "12345678",
    "userName": "测试用户",
    "userEmail": "test@example.com"
  }'
```

### 2. 用户登录
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  "http://localhost:8123/user/login" \
  -d '{
    "userAccount": "testuser",
    "userPassword": "12345678"
  }'
```

### 3. 发送邮箱验证码
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  "http://localhost:8123/user/send-code" \
  -d '{
    "email": "user@example.com",
    "purpose": "bind_email"
  }'
```

### 4. 绑定邮箱
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -H "satoken: Bearer YOUR_TOKEN_HERE" \
  "http://localhost:8123/user/bind-email" \
  -d '{
    "email": "user@example.com",
    "code": "123456"
  }'
```

### 5. 修改密码
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -H "satoken: Bearer YOUR_TOKEN_HERE" \
  "http://localhost:8123/user/change-password" \
  -d '{
    "oldPassword": "12345678",
    "newPassword": "newpassword123",
    "confirmPassword": "newpassword123"
  }'
```

### 6. 重置密码
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  "http://localhost:8123/user/reset-password" \
  -d '{
    "email": "user@example.com",
    "code": "123456",
    "newPassword": "newpassword123",
    "confirmPassword": "newpassword123"
  }'
```

## 验证码系统

### 验证码用途类型
- `bind_email`: 邮箱绑定
- `reset_password`: 重置密码
- `change_email`: 修改邮箱

### 验证码限制规则
1. **发送间隔**: 同一邮箱60秒内只能发送一次
2. **有效期**: 验证码5分钟内有效
3. **每日限制**: 每个邮箱每天最多发送10次
4. **验证码长度**: 6位数字

### 邮件模板特色
- 🎨 美观的HTML模板设计
- 📱 响应式布局，支持各种邮件客户端
- 🔒 安全提示和使用说明
- ⏰ 明确的有效期提醒

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

## 安全特性

### 1. 密码安全
- 使用 MD5 + 盐值加密
- 密码最小长度8位
- 不在响应中返回密码
- 修改密码需验证旧密码

### 2. 账号安全
- 账号唯一性校验
- 不允许特殊字符
- 账号最小长度4位

### 3. 邮箱安全
- 邮箱格式校验
- 邮箱唯一性校验（注册和更新时）
- 验证码防刷机制
- 多种验证码用途隔离

### 4. 接口安全
- Sa-Token 全局拦截
- 登录状态校验
- 用户状态检查（正常/禁用）
- 参数校验和长度限制

## 常见问题

### Q1: 为什么返回"未提供Token"？
A: 请检查header名称，应该使用 `satoken` 而不是 `Token`
```bash
# 正确方式
-H "satoken: Bearer YOUR_TOKEN_HERE"
```

### Q2: 邮箱验证码收不到怎么办？
A: 
1. 检查邮箱配置是否正确
2. 查看应用日志中的错误信息
3. 确认邮箱地址格式正确
4. 检查垃圾邮件箱

### Q3: 验证码显示过期怎么办？
A: 验证码有效期为5分钟，过期后需要重新发送

### Q4: 达到发送限制怎么办？
A: 
- 单次发送间隔：60秒
- 每日限制：10次
- 等待时间重置或联系管理员

## 项目结构

```
src/main/java/com/code/codeagent/
├── CodeAgentApplication.java      # 启动类
├── common/                        # 通用类
├── config/                       # 配置类
├── constant/                     # 常量类
│   ├── UserConstant.java         # 用户常量
│   └── MailConstant.java         # 邮件常量
├── controller/                   # 控制器
│   ├── UserController.java       # 用户控制器
│   └── AuthController.java       # 认证控制器
├── exception/                    # 异常处理
├── mapper/                       # 数据访问层
├── model/                        # 数据模型
│   ├── entity/                   # 实体类
│   ├── dto/                      # 数据传输对象
│   │   ├── SendCodeRequest.java  # 发送验证码请求
│   │   ├── BindEmailRequest.java # 绑定邮箱请求
│   │   ├── ChangePasswordRequest.java # 修改密码请求
│   │   └── ResetPasswordRequest.java # 重置密码请求
│   ├── vo/                       # 视图对象
│   └── enums/                    # 枚举类
└── service/                      # 服务层
    ├── MailService.java          # 邮件服务接口
    ├── UserService.java          # 用户服务接口
    └── impl/                     # 服务实现
        ├── MailServiceImpl.java  # 邮件服务实现
        └── UserServiceImpl.java  # 用户服务实现
```

## 依赖说明

### 核心依赖
- **Spring Boot Starter Web**: Web应用基础
- **Spring Boot Starter Mail**: 邮件发送服务
- **Spring Boot Starter Data Redis**: Redis缓存
- **Spring Boot Starter Validation**: 参数校验
- **MyBatis Plus**: ORM框架
- **Sa-Token**: 权限认证框架
- **HuTool**: Java工具类
- **Knife4j**: API文档

## 注意事项

1. **邮箱配置**: 确保邮箱服务配置正确，163邮箱需要使用授权码
2. **Redis**: 验证码存储依赖Redis，确保Redis服务正常
3. **Token使用**: 使用正确的header名称 `satoken`
4. **验证码限制**: 注意发送频率和每日限额
5. **密码安全**: 生产环境建议使用更强的加密算法

## 开发参考

- [Sa-Token 官方文档](https://sa-token.cc/)
- [MyBatis Plus 官方文档](https://baomidou.com/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Spring Mail 文档](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#mail)
- [Knife4j 文档](https://doc.xiaominfo.com/)

## License

MIT License 