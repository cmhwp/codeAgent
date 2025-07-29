-- 初始化管理员用户
USE code_agent;

-- 插入管理员用户
-- 账号：admin，密码：admin123456
-- 密码加密：MD5("codeagent" + "admin123456") = 7fef6171469e80d32c0559f88b377245
INSERT INTO user (userAccount, userPassword, userName, userRole, userStatus, createTime, updateTime, lastLoginTime, isDelete)
VALUES 
    ('admin', '7fef6171469e80d32c0559f88b377245', '系统管理员', 'admin', 0, NOW(), NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE 
    userPassword = '7fef6171469e80d32c0559f88b377245',
    userRole = 'admin',
    updateTime = NOW();

-- 插入测试用户
-- 账号：testuser，密码：testuser123
-- 密码加密：MD5("codeagent" + "testuser123") = 8e5689d68ba2bb0a3c0f8a4f5c6e9d2a
INSERT INTO user (userAccount, userPassword, userName, userRole, userStatus, createTime, updateTime, lastLoginTime, isDelete)
VALUES 
    ('testuser', '8e5689d68ba2bb0a3c0f8a4f5c6e9d2a', '测试用户', 'user', 0, NOW(), NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE 
    userPassword = '8e5689d68ba2bb0a3c0f8a4f5c6e9d2a',
    userRole = 'user',
    updateTime = NOW();

-- 查询验证
SELECT id, userAccount, userName, userRole, userStatus, createTime FROM user WHERE userAccount IN ('admin', 'testuser');

-- 说明：
-- 管理员账号：admin / admin123456
-- 测试用户：testuser / testuser123 