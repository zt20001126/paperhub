package org.paperhub.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.paperhub.auth.dto.LoginRequest;
import org.paperhub.auth.dto.RegisterRequest;
import org.paperhub.auth.dto.UpdateUserInfoRequest;
import org.paperhub.auth.entity.SysUser;
import org.paperhub.auth.mapper.AuthMapper;
import org.paperhub.auth.service.AuthService;
import org.paperhub.auth.service.EmailCodeService;
import org.paperhub.auth.vo.LoginUserVO;
import org.paperhub.exception.BizException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements AuthService {
    private static final ConcurrentHashMap<String, Long> TOKEN_USER_MAP = new ConcurrentHashMap<>();

    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailCodeService emailCodeService;

    public AuthServiceImpl(
            AuthMapper authMapper,
            PasswordEncoder passwordEncoder,
            EmailCodeService emailCodeService) {
        this.authMapper = authMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailCodeService = emailCodeService;
    }

    /**
     * Send register verification code to email.
     */
    @Override
    public void sendRegisterCode(String email) {
        SysUser user = findByEmail(email);
        if (user != null) {
            throw new BizException("该邮箱已注册");
        }
        emailCodeService.sendCode(email);
    }

    /**
     * Register a new user.
     */
    @Override
    public void register(RegisterRequest request) {
        SysUser existing = findByEmail(request.getEmail());
        if (existing != null) {
            throw new BizException("该邮箱已注册");
        }
        emailCodeService.verifyCode(request.getEmail(), request.getCode());

        SysUser newUser = new SysUser();
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setNickname(defaultNickname(request.getEmail()));
        newUser.setStatus(1);
        newUser.setPoints(100);
        authMapper.insert(newUser);
    }

    /**
     * Login and return user info + token.
     */
    @Override
    public LoginUserVO login(LoginRequest request) {
        SysUser user = findByEmail(request.getEmail());
        if (user == null) {
            throw new BizException("账号不存在");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BizException("账号已被禁用");
        }
        if (!verifyPassword(user, request.getPassword())) {
            throw new BizException("邮箱或密码错误");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        TOKEN_USER_MAP.put(token, user.getId());

        LoginUserVO vo = new LoginUserVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setPoints(user.getPoints());
        vo.setToken(token);
        return vo;
    }

    /**
     * Update current user's profile by token.
     */
    @Override
    public void updateUserInfo(String token, UpdateUserInfoRequest request) {
        Long userId = TOKEN_USER_MAP.get(token);
        if (userId == null) {
            throw new BizException("登录状态失效，请重新登录");
        }

        SysUser user = authMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        boolean hasUpdate = false;
        if (StringUtils.hasText(request.getNickname())) {
            String nickname = request.getNickname().trim();
            if (nickname.length() < 2 || nickname.length() > 20) {
                throw new BizException("昵称长度需在2-20个字符");
            }
            user.setNickname(nickname);
            hasUpdate = true;
        }
        if (StringUtils.hasText(request.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
            hasUpdate = true;
        }

        if (!hasUpdate) {
            throw new BizException("没有可更新的信息");
        }

        authMapper.updateById(user);
    }

    private boolean verifyPassword(SysUser user, String rawPassword) {
        String storedPassword = user.getPassword();
        if (storedPassword == null || storedPassword.isEmpty()) {
            return false;
        }

        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        if (!storedPassword.equals(rawPassword)) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(rawPassword));
        authMapper.updateById(user);
        return true;
    }

    private SysUser findByEmail(String email) {
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
        query.eq(SysUser::getEmail, email).last("LIMIT 1");
        return authMapper.selectOne(query);
    }

    private String defaultNickname(String email) {
        int index = email.indexOf('@');
        if (index <= 0) {
            return "paperhub_user";
        }
        return email.substring(0, index);
    }
}
