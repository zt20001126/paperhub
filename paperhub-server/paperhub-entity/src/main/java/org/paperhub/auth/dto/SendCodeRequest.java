package org.paperhub.auth.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class SendCodeRequest {
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "图片验证码ID不能为空")
    private String captchaId;

    @NotBlank(message = "图片验证码不能为空")
    private String captchaCode;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getCaptchaCode() {
        return captchaCode;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }
}
