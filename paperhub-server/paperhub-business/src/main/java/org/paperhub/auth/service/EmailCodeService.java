package org.paperhub.auth.service;

import org.paperhub.auth.constant.AuthRedisKeys;
import org.paperhub.config.AuthProperties;
import org.paperhub.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class EmailCodeService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String EMAIL_CODE_ERROR_MESSAGE = "邮箱验证码错误或已过期";

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final AuthProperties authProperties;
    private final String from;

    public EmailCodeService(
            JavaMailSender mailSender,
            StringRedisTemplate redisTemplate,
            AuthProperties authProperties,
            @Value("${spring.mail.username}") String from) {
        this.mailSender = mailSender;
        this.redisTemplate = redisTemplate;
        this.authProperties = authProperties;
        this.from = from;
    }

    public void sendCode(String email) {
        String cooldownKey = AuthRedisKeys.registerEmailCodeCooldown(email);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            throw new BizException("发送过于频繁，请稍后再试");
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        // 先发送邮件，发送成功后再写入 Redis，避免用户收到不可校验的验证码。
        sendMail(email, code);

        // 邮箱验证码和发送冷却分开存储：验证码控制有效期，冷却 Key 控制重复发送频率。
        redisTemplate.opsForValue().set(
                AuthRedisKeys.registerEmailCode(email),
                code,
                authProperties.getEmailCode().getExpireSeconds(),
                TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(
                cooldownKey,
                "1",
                authProperties.getEmailCode().getCooldownSeconds(),
                TimeUnit.SECONDS);
        redisTemplate.delete(AuthRedisKeys.registerEmailCodeFail(email));
    }

    public void verifyCode(String email, String code) {
        // 邮箱验证码只用于注册流程，校验成功后会被删除，不能重复使用。
        if (!StringUtils.hasText(email) || !StringUtils.hasText(code)) {
            throw new BizException(EMAIL_CODE_ERROR_MESSAGE);
        }

        String key = AuthRedisKeys.registerEmailCode(email);
        String storedCode = redisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(storedCode)) {
            throw new BizException(EMAIL_CODE_ERROR_MESSAGE);
        }

        if (!storedCode.equals(code.trim())) {
            // 邮箱验证码输错次数达到阈值后清除验证码，要求用户重新获取。
            increaseFailCount(email);
            throw new BizException(EMAIL_CODE_ERROR_MESSAGE);
        }

        redisTemplate.delete(key);
        redisTemplate.delete(AuthRedisKeys.registerEmailCodeFail(email));
    }

    private void sendMail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("PaperHub 注册验证码");
        message.setText("您的 PaperHub 注册验证码为：" + code + "，"
                + authProperties.getEmailCode().getExpireSeconds() / 60 + "分钟内有效。");
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new BizException("邮件发送失败，请稍后再试");
        }
    }

    private void increaseFailCount(String email) {
        String failKey = AuthRedisKeys.registerEmailCodeFail(email);
        Long count = redisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1L) {
            // 失败计数跟随验证码有效期自动过期，避免过期验证码留下无意义的失败记录。
            redisTemplate.expire(failKey, authProperties.getEmailCode().getExpireSeconds(), TimeUnit.SECONDS);
        }
        if (count != null && count >= authProperties.getEmailCode().getMaxFailCount()) {
            redisTemplate.delete(AuthRedisKeys.registerEmailCode(email));
            redisTemplate.delete(failKey);
        }
    }
}
