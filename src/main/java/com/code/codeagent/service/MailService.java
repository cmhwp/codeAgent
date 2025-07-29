package com.code.codeagent.service;

/**
 * 邮件服务接口
 *
 * @author CodeAgent
 */
public interface MailService {

    /**
     * 发送验证码邮件
     *
     * @param email   收件人邮箱
     * @param purpose 验证码用途
     * @return 是否发送成功
     */
    boolean sendVerificationCode(String email, String purpose);

    /**
     * 验证邮箱验证码
     *
     * @param email   邮箱
     * @param code    验证码
     * @param purpose 验证码用途
     * @return 是否验证成功
     */
    boolean verifyCode(String email, String code, String purpose);

    /**
     * 发送普通邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容（HTML格式）
     * @return 是否发送成功
     */
    boolean sendMail(String to, String subject, String content);
} 