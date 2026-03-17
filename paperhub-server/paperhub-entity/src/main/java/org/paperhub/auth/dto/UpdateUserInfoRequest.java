package org.paperhub.auth.dto;

import javax.validation.constraints.Size;

public class UpdateUserInfoRequest {
    @Size(max = 255, message = "头像地址过长")
    private String avatarUrl;

    @Size(min = 2, max = 20, message = "昵称长度需在2-20个字符")
    private String nickname;

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
