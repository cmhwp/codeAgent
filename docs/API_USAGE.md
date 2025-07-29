# API 使用示例

## 邮箱验证码和密码功能完整指南

### 🔧 Sa-Token 配置说明

根据项目配置，Sa-Token的参数如下：
- **Token名称**: `satoken`
- **Token前缀**: `Bearer`
- **Header读取**: 启用

### 📝 正确的请求方式

#### 方式一：使用satoken header（推荐）
```bash
curl -X GET \
  -H "Accept: application/json" \
  -H "satoken: Bearer YOUR_TOKEN_HERE" \
  "http://localhost:8123/user/get/login"
```

#### 方式二：使用Authorization header
```bash
curl -X GET \
  -H "Accept: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  "http://localhost:8123/user/get/login"
```

### 🚀 完整的使用流程

#### 1. 用户注册
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

#### 2. 用户登录
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  "http://localhost:8123/user/login" \
  -d '{
    "userAccount": "testuser",
    "userPassword": "12345678"
  }'
```

**响应示例：**
```json
{
  "code": 0,
  "data": {
    "id": 1,
    "userAccount": "testuser",
    "userName": "测试用户",
    "token": "994b0d3a-640a-42ee-8e3f-25e6a7688e44"
  },
  "message": "ok"
}
```

#### 3. 获取当前登录用户信息
```bash
curl -X GET \
  -H "Accept: application/json" \
  -H "satoken: Bearer 994b0d3a-640a-42ee-8e3f-25e6a7688e44" \
  "http://localhost:8123/user/get/login"
```

### 📧 邮箱验证码功能

#### 4. 发送邮箱验证码
```bash
# 绑定邮箱验证码
curl -X POST \
  -H "Content-Type: application/json" \
  "http://localhost:8123/user/send-code" \
  -d '{
    "email": "user@example.com",
    "purpose": "bind_email"
  }'

# 重置密码验证码
curl -X POST \
  -H "Content-Type: application/json" \
  "http://localhost:8123/user/send-code" \
  -d '{
    "email": "user@example.com",
    "purpose": "reset_password"
  }'
```

**验证码用途类型：**
- `bind_email`: 邮箱绑定
- `reset_password`: 重置密码
- `change_email`: 修改邮箱

#### 5. 绑定邮箱（需要登录）
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

### 🔒 密码管理功能

#### 6. 修改密码（需要登录）
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

#### 7. 重置密码（忘记密码时使用）
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

#### 8. 更新个人信息（需要登录）
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -H "satoken: Bearer YOUR_TOKEN_HERE" \
  "http://localhost:8123/user/update/my" \
  -d '{
    "userName": "新昵称",
    "userAvatar": "https://example.com/avatar.jpg",
    "userProfile": "这是我的个人简介",
    "userEmail": "newemail@example.com"
  }'
```

### ⚠️ 常见错误及解决方案

#### 错误1：未提供Token
```json
{
  "code": 40101,
  "message": "未提供Token"
}
```
**解决方案**: 使用正确的header名称 `satoken` 而不是 `Token`

#### 错误2：Token无效
```json
{
  "code": 40101,
  "message": "Token无效"
}
```
**解决方案**: 
1. 检查token是否正确
2. 检查token是否已过期
3. 重新登录获取新token

#### 错误3：验证码相关错误
```json
{
  "code": 40000,
  "message": "验证码错误或已过期"
}
```
**解决方案**: 
1. 检查验证码是否正确
2. 验证码有效期为5分钟
3. 重新发送验证码

### 🔄 验证码限制规则

1. **发送间隔**: 同一邮箱60秒内只能发送一次
2. **有效期**: 验证码5分钟内有效
3. **每日限制**: 每个邮箱每天最多发送10次
4. **验证码长度**: 6位数字

### 📱 前端集成示例

#### JavaScript/Axios 示例
```javascript
// 登录并获取token
const loginResponse = await axios.post('/user/login', {
  userAccount: 'testuser',
  userPassword: '12345678'
});

const token = loginResponse.data.data.token;

// 设置axios默认header
axios.defaults.headers.common['satoken'] = `Bearer ${token}`;

// 或者单独设置
const config = {
  headers: {
    'satoken': `Bearer ${token}`
  }
};

// 获取用户信息
const userInfo = await axios.get('/user/get/login', config);
```

### 🏥 健康检查

测试邮件服务是否正常：
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  "http://localhost:8123/user/send-code" \
  -d '{
    "email": "test@example.com",
    "purpose": "bind_email"
  }'
```

如果返回成功，说明邮件服务配置正确。

---

## 总结

您之前的curl请求问题在于使用了错误的header名称。正确的方式是：

**❌ 错误的方式：**
```bash
-H "Token:994b0d3a-640a-42ee-8e3f-25e6a7688e44"
```

**✅ 正确的方式：**
```bash
-H "satoken: Bearer 994b0d3a-640a-42ee-8e3f-25e6a7688e44"
```

现在您的项目已经集成了完整的邮箱验证码和密码管理功能！ 