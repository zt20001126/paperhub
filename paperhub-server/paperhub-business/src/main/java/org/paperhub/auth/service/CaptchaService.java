package org.paperhub.auth.service;

import org.paperhub.auth.vo.CaptchaVO;
import org.paperhub.exception.BizException;
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
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 4;
    private static final int EXPIRE_SECONDS = 300;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 44;

    private final Map<String, CaptchaRecord> captchaStore = new ConcurrentHashMap<>();

    public CaptchaVO createCaptcha() {
        cleanupExpiredRecords();

        String code = randomCode();
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expireAt = LocalDateTime.now().plusSeconds(EXPIRE_SECONDS);
        captchaStore.put(captchaId, new CaptchaRecord(code, expireAt));

        CaptchaVO vo = new CaptchaVO();
        vo.setCaptchaId(captchaId);
        vo.setImageBase64("data:image/png;base64," + renderPngBase64(code));
        vo.setExpiresInSeconds(EXPIRE_SECONDS);
        return vo;
    }

    public void verify(String captchaId, String captchaCode, boolean removeOnSuccess) {
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)) {
            throw new BizException("图片验证码不能为空");
        }

        CaptchaRecord record = captchaStore.get(captchaId);
        if (record == null) {
            throw new BizException("图片验证码不存在或已过期");
        }
        if (LocalDateTime.now().isAfter(record.expireAt)) {
            captchaStore.remove(captchaId);
            throw new BizException("图片验证码已过期，请刷新后重试");
        }
        if (!record.code.equalsIgnoreCase(captchaCode.trim())) {
            throw new BizException("图片验证码错误");
        }
        if (removeOnSuccess) {
            captchaStore.remove(captchaId);
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

    private void cleanupExpiredRecords() {
        LocalDateTime now = LocalDateTime.now();
        captchaStore.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expireAt));
    }

    private static class CaptchaRecord {
        private final String code;
        private final LocalDateTime expireAt;

        private CaptchaRecord(String code, LocalDateTime expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }
}
