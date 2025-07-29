package com.code.codeagent.constant;

/**
 * 邮件相关常量
 *
 * @author CodeAgent
 */
public interface MailConstant {

    /**
     * 验证码有效期（分钟）
     */
    int VERIFICATION_CODE_EXPIRE_MINUTES = 5;

    /**
     * 验证码长度
     */
    int VERIFICATION_CODE_LENGTH = 6;

    /**
     * 同一邮箱发送验证码间隔时间（秒）
     */
    int SEND_CODE_INTERVAL_SECONDS = 60;

    /**
     * 每日单个邮箱最大发送次数
     */
    int MAX_SEND_COUNT_PER_DAY = 10;

    /**
     * Redis key前缀
     */
    interface RedisKey {
        /**
         * 邮箱验证码 key前缀：email_code:{email}
         */
        String EMAIL_CODE_PREFIX = "email_code:";

        /**
         * 邮箱发送次数 key前缀：email_send_count:{email}:{date}
         */
        String EMAIL_SEND_COUNT_PREFIX = "email_send_count:";

        /**
         * 邮箱发送间隔 key前缀：email_send_interval:{email}
         */
        String EMAIL_SEND_INTERVAL_PREFIX = "email_send_interval:";
    }

    /**
     * 验证码用途
     */
    interface Purpose {
        /**
         * 邮箱绑定
         */
        String BIND_EMAIL = "bind_email";

        /**
         * 重置密码
         */
        String RESET_PASSWORD = "reset_password";

        /**
         * 修改邮箱
         */
        String CHANGE_EMAIL = "change_email";
    }

    /**
     * 邮件模板
     */
    interface Template {
        /**
         * 验证码邮件主题
         */
        String VERIFICATION_CODE_SUBJECT = "【CodeAgent】邮箱验证码";

        /**
         * 验证码邮件内容模板
         */
        String VERIFICATION_CODE_CONTENT = """
                <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; font-family: 'Helvetica Neue', Arial, sans-serif;">
                    <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 15px; padding: 40px; box-shadow: 0 10px 30px rgba(0,0,0,0.1);">
                        <div style="text-align: center; margin-bottom: 30px;">
                            <h1 style="color: #333; font-size: 28px; margin: 0; font-weight: 300;">CodeAgent</h1>
                            <p style="color: #666; font-size: 16px; margin: 10px 0 0 0;">专业的代码开发平台</p>
                        </div>
                        
                        <div style="background: #f8f9fa; border-radius: 10px; padding: 25px; margin: 20px 0;">
                            <h2 style="color: #333; font-size: 20px; margin: 0 0 15px 0;">邮箱验证码</h2>
                            <p style="color: #666; font-size: 14px; line-height: 1.6; margin: 0 0 20px 0;">
                                您正在进行<strong style="color: #667eea;">%s</strong>操作，验证码为：
                            </p>
                            <div style="text-align: center; margin: 20px 0;">
                                <span style="display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; font-size: 32px; font-weight: bold; padding: 15px 30px; border-radius: 8px; letter-spacing: 8px; font-family: 'Courier New', monospace;">%s</span>
                            </div>
                            <p style="color: #999; font-size: 12px; text-align: center; margin: 15px 0 0 0;">
                                验证码有效期为 <strong>%d 分钟</strong>，请尽快使用
                            </p>
                        </div>
                        
                        <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;">
                            <p style="color: #999; font-size: 12px; line-height: 1.5; margin: 0;">
                                • 如果这不是您的操作，请忽略此邮件<br>
                                • 请勿将验证码告知他人，以保护您的账户安全<br>
                                • 此邮件由系统自动发送，请勿回复
                            </p>
                        </div>
                        
                        <div style="text-align: center; margin-top: 30px;">
                            <p style="color: #ccc; font-size: 12px; margin: 0;">
                                © 2024 CodeAgent. All rights reserved.
                            </p>
                        </div>
                    </div>
                </div>
                """;
    }
} 