package org.paperhub.auth.service;

import org.paperhub.auth.constant.AuthRedisKeys;
import org.paperhub.config.AuthProperties;
import org.paperhub.exception.BizException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginFailService {
    private final StringRedisTemplate redisTemplate;
    private final AuthProperties authProperties;

    public LoginFailService(StringRedisTemplate redisTemplate, AuthProperties authProperties) {
        this.redisTemplate = redisTemplate;
        this.authProperties = authProperties;
    }

    public void checkLocked(String account) {
        String value = redisTemplate.opsForValue().get(AuthRedisKeys.loginFail(account));
        if (value == null) {
            return;
        }
        int failCount = parseInt(value);
        if (failCount >= authProperties.getLogin().getMaxFailCount()) {
            throw new BizException("登录失败次数过多，请稍后再试");
        }
    }

    public void recordFail(String account) {
        String key = AuthRedisKeys.loginFail(account);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, authProperties.getLogin().getLockSeconds(), TimeUnit.SECONDS);
        }
        if (count != null && count >= authProperties.getLogin().getMaxFailCount()) {
            redisTemplate.expire(key, authProperties.getLogin().getLockSeconds(), TimeUnit.SECONDS);
        }
    }

    public void clearFail(String account) {
        redisTemplate.delete(AuthRedisKeys.loginFail(account));
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
