package org.paperhub.auth.service;

import org.paperhub.auth.dto.LoginRequest;
import org.paperhub.auth.dto.RegisterRequest;
import org.paperhub.auth.vo.LoginUserVO;

public interface AuthService {
    void sendRegisterCode(String email);

    void register(RegisterRequest request);

    LoginUserVO login(LoginRequest request);
}
