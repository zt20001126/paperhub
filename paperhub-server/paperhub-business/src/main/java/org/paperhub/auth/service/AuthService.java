package org.paperhub.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.paperhub.auth.mapper.SysUserMapper;
import org.paperhub.exception.BizException;
import org.paperhub.auth.entity.SysUser;
import org.paperhub.auth.dto.LoginRequest;
import org.paperhub.auth.dto.RegisterRequest;
import org.paperhub.auth.vo.LoginUserVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailCodeService emailCodeService;

    public AuthService(SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder, EmailCodeService emailCodeService) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailCodeService = emailCodeService;
    }

    public void sendRegisterCode(String email) {
        SysUser user = findByEmail(email);
        if (user != null) {
            throw new BizException("该邮箱已注册");
        }
        emailCodeService.sendCode(email);
    }

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
        sysUserMapper.insert(newUser);
    }

    public LoginUserVO login(LoginRequest request) {
        SysUser user = findByEmail(request.getEmail());
        if (user == null) {
            throw new BizException("账号不存在");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BizException("账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException("邮箱或密码错误");
        }

        LoginUserVO vo = new LoginUserVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        vo.setToken(UUID.randomUUID().toString().replace("-", ""));
        return vo;
    }

    private SysUser findByEmail(String email) {
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
        query.eq(SysUser::getEmail, email).last("LIMIT 1");
        return sysUserMapper.selectOne(query);
    }

    private String defaultNickname(String email) {
        int index = email.indexOf('@');
        if (index <= 0) {
            return "paperhub_user";
        }
        return email.substring(0, index);
    }
}
