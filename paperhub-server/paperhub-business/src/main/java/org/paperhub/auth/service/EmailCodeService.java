package org.paperhub.auth.service;

import org.paperhub.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailCodeService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int EXPIRE_MINUTES = 5;

    private final JavaMailSender mailSender;
    private final String from;
    private final Map<String, CodeRecord> codeStore = new ConcurrentHashMap<>();

    public EmailCodeService(JavaMailSender mailSender, @Value("${spring.mail.username}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendCode(String email) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(EXPIRE_MINUTES);
        codeStore.put(email, new CodeRecord(code, expireAt));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("PaperHub 注册验证码");
        message.setText("您的 PaperHub 注册验证码为：" + code + "，" + EXPIRE_MINUTES + "分钟内有效。");
        mailSender.send(message);
    }

    public void verifyCode(String email, String code) {
        CodeRecord record = codeStore.get(email);
        if (record == null) {
            throw new BizException("请先获取验证码");
        }
        if (LocalDateTime.now().isAfter(record.expireAt)) {
            codeStore.remove(email);
            throw new BizException("验证码已过期，请重新获取");
        }
        if (!record.code.equals(code)) {
            throw new BizException("验证码错误");
        }
        codeStore.remove(email);
    }

    private static class CodeRecord {
        private final String code;
        private final LocalDateTime expireAt;

        private CodeRecord(String code, LocalDateTime expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }
}
