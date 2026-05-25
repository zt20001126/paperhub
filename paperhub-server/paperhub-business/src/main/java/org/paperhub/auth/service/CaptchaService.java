package org.paperhub.auth.service;

import org.paperhub.auth.constant.AuthRedisKeys;
import org.paperhub.auth.vo.CaptchaVO;
import org.paperhub.config.AuthProperties;
import org.paperhub.exception.BizException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CaptchaService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 4;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 44;
    private static final String CAPTCHA_ERROR_MESSAGE = "图形验证码错误或已过期";

    private final StringRedisTemplate redisTemplate;
    private final AuthProperties authProperties;

    public CaptchaService(StringRedisTemplate redisTemplate, AuthProperties authProperties) {
        this.redisTemplate = redisTemplate;
        this.authProperties = authProperties;
    }

    public CaptchaVO createCaptcha() {
        String code = randomCode();
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        int expireSeconds = authProperties.getCaptcha().getExpireSeconds();

        redisTemplate.opsForValue().set(
                AuthRedisKeys.captcha(captchaId),
                code.toLowerCase(),
                expireSeconds,
                TimeUnit.SECONDS);

        CaptchaVO vo = new CaptchaVO();
        vo.setCaptchaId(captchaId);
        vo.setImageBase64("data:image/png;base64," + renderPngBase64(code));
        vo.setExpiresInSeconds(expireSeconds);
        return vo;
    }

    public void verify(String captchaId, String captchaCode) {
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)) {
            throw new BizException(CAPTCHA_ERROR_MESSAGE);
        }

        String key = AuthRedisKeys.captcha(captchaId);
        String storedCode = redisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(storedCode)) {
            throw new BizException(CAPTCHA_ERROR_MESSAGE);
        }

        String inputCode = captchaCode.trim().toLowerCase();
        if (!storedCode.equals(inputCode)) {
            increaseFailCount(captchaId);
            throw new BizException(CAPTCHA_ERROR_MESSAGE);
        }

        redisTemplate.delete(key);
        redisTemplate.delete(AuthRedisKeys.captchaFail(captchaId));
    }

    public void verify(String captchaId, String captchaCode, boolean removeOnSuccess) {
        verify(captchaId, captchaCode);
    }

    private void increaseFailCount(String captchaId) {
        String failKey = AuthRedisKeys.captchaFail(captchaId);
        Long count = redisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(failKey, authProperties.getCaptcha().getExpireSeconds(), TimeUnit.SECONDS);
        }
        if (count != null && count >= authProperties.getCaptcha().getMaxFailCount()) {
            redisTemplate.delete(AuthRedisKeys.captcha(captchaId));
            redisTemplate.delete(failKey);
        }
    }

    private String randomCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return builder.toString();
    }

    private String renderPngBase64(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(245, 247, 250));
            graphics.fillRect(0, 0, WIDTH, HEIGHT);

            for (int i = 0; i < 8; i++) {
                graphics.setColor(randomMutedColor());
                int x1 = RANDOM.nextInt(WIDTH);
                int y1 = RANDOM.nextInt(HEIGHT);
                int x2 = RANDOM.nextInt(WIDTH);
                int y2 = RANDOM.nextInt(HEIGHT);
                graphics.drawLine(x1, y1, x2, y2);
            }

            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
            for (int i = 0; i < code.length(); i++) {
                graphics.setColor(randomTextColor());
                int x = 18 + i * 24;
                int y = 30 + RANDOM.nextInt(7) - 3;
                double angle = Math.toRadians(RANDOM.nextInt(21) - 10);
                graphics.rotate(angle, x, y);
                graphics.drawString(String.valueOf(code.charAt(i)), x, y);
                graphics.rotate(-angle, x, y);
            }

            for (int i = 0; i < 40; i++) {
                graphics.setColor(randomMutedColor());
                graphics.fillOval(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT), 2, 2);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            throw new BizException("图片验证码生成失败");
        } finally {
            graphics.dispose();
        }
    }

    private Color randomTextColor() {
        return new Color(30 + RANDOM.nextInt(80), 45 + RANDOM.nextInt(80), 60 + RANDOM.nextInt(80));
    }

    private Color randomMutedColor() {
        return new Color(140 + RANDOM.nextInt(80), 150 + RANDOM.nextInt(80), 160 + RANDOM.nextInt(80));
    }
}
