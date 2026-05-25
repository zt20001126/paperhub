package org.paperhub.auth.constant;

public final class AuthRedisKeys {
    private AuthRedisKeys() {
    }

    public static String captcha(String captchaId) {
        return "auth:captcha:" + captchaId;
    }

    public static String captchaFail(String captchaId) {
        return "auth:captcha:fail:" + captchaId;
    }

    public static String registerEmailCode(String email) {
        return "auth:email-code:register:" + normalize(email);
    }

    public static String registerEmailCodeFail(String email) {
        return "auth:email-code:fail:register:" + normalize(email);
    }

    public static String registerEmailCodeCooldown(String email) {
        return "auth:email-code:cooldown:register:" + normalize(email);
    }

    public static String loginFail(String account) {
        return "auth:login:fail:" + normalize(account);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
