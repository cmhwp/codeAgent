package com.code.codeagent.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.code.codeagent.constant.MailConstant;
import com.code.codeagent.exception.BusinessException;
import com.code.codeagent.exception.ErrorCode;
import com.code.codeagent.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.TimeUnit;

/**
 * 邮件服务实现类
 *
 * @author CodeAgent
 */
@Service
@Slf4j
public class MailServiceImpl implements MailService {

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public boolean sendVerificationCode(String email, String purpose) {
        // 1. 参数校验
        if (StrUtil.hasBlank(email, purpose)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱或用途不能为空");
        }

        // 2. 检查发送间隔
        String intervalKey = MailConstant.RedisKey.EMAIL_SEND_INTERVAL_PREFIX + email;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(intervalKey))) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "发送过于频繁，请稍后再试");
        }

        // 3. 检查当日发送次数
        String today = DateUtil.today();
        String countKey = MailConstant.RedisKey.EMAIL_SEND_COUNT_PREFIX + email + ":" + today;
        String countStr = stringRedisTemplate.opsForValue().get(countKey);
        int sendCount = countStr != null ? Integer.parseInt(countStr) : 0;
        if (sendCount >= MailConstant.MAX_SEND_COUNT_PER_DAY) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "今日发送次数已达上限");
        }

        // 4. 生成验证码
        String code = RandomUtil.randomNumbers(MailConstant.VERIFICATION_CODE_LENGTH);

        // 5. 发送邮件
        String purposeText = getPurposeText(purpose);
        String subject = MailConstant.Template.VERIFICATION_CODE_SUBJECT;
        String content = String.format(MailConstant.Template.VERIFICATION_CODE_CONTENT, 
                purposeText, code, MailConstant.VERIFICATION_CODE_EXPIRE_MINUTES);

        boolean success = sendMail(email, subject, content);
        
        if (success) {
            // 6. 存储验证码到Redis
            String codeKey = MailConstant.RedisKey.EMAIL_CODE_PREFIX + email + ":" + purpose;
            stringRedisTemplate.opsForValue().set(codeKey, code, 
                    MailConstant.VERIFICATION_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

            // 7. 设置发送间隔
            stringRedisTemplate.opsForValue().set(intervalKey, "1", 
                    MailConstant.SEND_CODE_INTERVAL_SECONDS, TimeUnit.SECONDS);

            // 8. 增加发送次数
            stringRedisTemplate.opsForValue().increment(countKey);
            stringRedisTemplate.expire(countKey, 1, TimeUnit.DAYS);

            log.info("验证码发送成功，邮箱：{}，用途：{}", email, purpose);
        }

        return success;
    }

    @Override
    public boolean verifyCode(String email, String code, String purpose) {
        if (StrUtil.hasBlank(email, code, purpose)) {
            return false;
        }

        String codeKey = MailConstant.RedisKey.EMAIL_CODE_PREFIX + email + ":" + purpose;
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);

        if (StrUtil.isBlank(storedCode)) {
            return false;
        }

        boolean isValid = code.equals(storedCode);
        if (isValid) {
            // 验证成功后删除验证码
            stringRedisTemplate.delete(codeKey);
            log.info("验证码验证成功，邮箱：{}，用途：{}", email, purpose);
        } else {
            log.warn("验证码验证失败，邮箱：{}，用途：{}，输入验证码：{}", email, purpose, code);
        }

        return isValid;
    }

    @Override
    public boolean sendMail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true表示HTML格式

            mailSender.send(message);
            log.info("邮件发送成功，收件人：{}，主题：{}", to, subject);
            return true;
        } catch (Exception e) {
            log.error("邮件发送失败，收件人：{}，主题：{}，错误：{}", to, subject, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取用途的中文描述
     */
    private String getPurposeText(String purpose) {
        return switch (purpose) {
            case MailConstant.Purpose.BIND_EMAIL -> "邮箱绑定";
            case MailConstant.Purpose.RESET_PASSWORD -> "密码重置";
            case MailConstant.Purpose.CHANGE_EMAIL -> "邮箱修改";
            default -> "身份验证";
        };
    }
} 