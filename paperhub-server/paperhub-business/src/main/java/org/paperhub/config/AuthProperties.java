package org.paperhub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "paperhub.auth")
public class AuthProperties {
    private final Captcha captcha = new Captcha();
    private final EmailCode emailCode = new EmailCode();
    private final Login login = new Login();
    private final Password password = new Password();

    public Captcha getCaptcha() {
        return captcha;
    }

    public EmailCode getEmailCode() {
        return emailCode;
    }

    public Login getLogin() {
        return login;
    }

    public Password getPassword() {
        return password;
    }

    public static class Captcha {
        private int expireSeconds = 300;
        private int maxFailCount = 5;

        public int getExpireSeconds() {
            return expireSeconds;
        }

        public void setExpireSeconds(int expireSeconds) {
            this.expireSeconds = expireSeconds;
        }

        public int getMaxFailCount() {
            return maxFailCount;
        }

        public void setMaxFailCount(int maxFailCount) {
            this.maxFailCount = maxFailCount;
        }
    }

    public static class EmailCode {
        private int expireSeconds = 300;
        private int cooldownSeconds = 60;
        private int maxFailCount = 5;

        public int getExpireSeconds() {
            return expireSeconds;
        }

        public void setExpireSeconds(int expireSeconds) {
            this.expireSeconds = expireSeconds;
        }

        public int getCooldownSeconds() {
            return cooldownSeconds;
        }

        public void setCooldownSeconds(int cooldownSeconds) {
            this.cooldownSeconds = cooldownSeconds;
        }

        public int getMaxFailCount() {
            return maxFailCount;
        }

        public void setMaxFailCount(int maxFailCount) {
            this.maxFailCount = maxFailCount;
        }
    }

    public static class Login {
        private int maxFailCount = 5;
        private int lockSeconds = 900;

        public int getMaxFailCount() {
            return maxFailCount;
        }

        public void setMaxFailCount(int maxFailCount) {
            this.maxFailCount = maxFailCount;
        }

        public int getLockSeconds() {
            return lockSeconds;
        }

        public void setLockSeconds(int lockSeconds) {
            this.lockSeconds = lockSeconds;
        }
    }

    public static class Password {
        private int minLength = 8;

        public int getMinLength() {
            return minLength;
        }

        public void setMinLength(int minLength) {
            this.minLength = minLength;
        }
    }
}
