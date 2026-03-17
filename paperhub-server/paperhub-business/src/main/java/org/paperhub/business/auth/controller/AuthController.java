package org.paperhub.business.auth.controller;

import org.paperhub.business.auth.service.AuthService;
import org.paperhub.common.result.Result;
import org.paperhub.entity.auth.dto.LoginRequest;
import org.paperhub.entity.auth.dto.RegisterRequest;
import org.paperhub.entity.auth.dto.SendCodeRequest;
import org.paperhub.entity.auth.vo.LoginUserVO;

import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        authService.sendRegisterCode(request.getEmail());
        return Result.ok(null);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.ok(null);
    }

    @PostMapping("/login")
    public Result<LoginUserVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }
}
