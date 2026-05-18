package org.paperhub.auth.service;

import org.paperhub.auth.dto.LoginRequest;
import org.paperhub.auth.dto.RegisterRequest;
import org.paperhub.auth.dto.SendCodeRequest;
import org.paperhub.auth.dto.UpdateUserInfoRequest;
import org.paperhub.auth.vo.CaptchaVO;
import org.paperhub.auth.vo.LoginUserVO;

public interface AuthService {
    /**
     * Create image captcha.
     */
    CaptchaVO captcha();

    /**
     * Send register verification code to email.
     */
    void sendRegisterCode(SendCodeRequest request);

    /**
     * Register a new user.
     */
    void register(RegisterRequest request);

    /**
     * Login and return user info + token.
     */
    LoginUserVO login(LoginRequest request);

    /**
     * Update current user's profile by token.
     */
    void updateUserInfo(String token, UpdateUserInfoRequest request);
}
