-- 初始测试数据
USE code_agent;

-- 插入管理员用户（默认密码：admin123456）
-- 密码使用MD5加盐加密：MD5("codeagent" + "admin123456") = 加密后的值
INSERT INTO user (userAccount, userPassword, userName, userRole, userStatus, createTime, updateTime, lastLoginTime, isDelete)
VALUES 
    ('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'admin', 0, NOW(), NOW(), NOW(), 0),
    ('testuser', 'e10adc3949ba59abbe56e057f20f883e', '测试用户', 'user', 0, NOW(), NOW(), NOW(), 0);

-- 注意：上面的密码hash是示例，实际使用时需要用正确的加密值
-- 正确的加密方式：MD5("codeagent" + "admin123456")

-- 查询验证
SELECT userAccount, userName, userRole, userStatus, createTime FROM user; 