package org.paperhub.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.paperhub.auth.dto.LoginRequest;
import org.paperhub.auth.dto.RegisterRequest;
import org.paperhub.auth.entity.SysUser;
import org.paperhub.auth.mapper.AuthMapper;
import org.paperhub.auth.service.AuthService;
import org.paperhub.auth.service.EmailCodeService;
import org.paperhub.auth.vo.LoginUserVO;
import org.paperhub.exception.BizException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
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
     * 发送邮箱注册验证码，并校验邮箱是否已被注册。
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
     * 校验验证码并完成新用户注册。
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
     * 校验账号状态与密码，登录成功后返回用户信息和临时令牌。
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

        LoginUserVO vo = new LoginUserVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setPoints(user.getPoints());
        vo.setToken(UUID.randomUUID().toString().replace("-", ""));
        return vo;
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
