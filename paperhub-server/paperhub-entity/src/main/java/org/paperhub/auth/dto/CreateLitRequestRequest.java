package org.paperhub.auth.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CreateLitRequestRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "论文标题不能为空")
    private String title;

    @NotBlank(message = "期刊不能为空")
    private String journal;

    @NotBlank(message = "DOI不能为空")
    private String doi;

    @NotNull(message = "奖励积分不能为空")
    @Min(value = 0, message = "奖励积分不能小于0")
    private Integer rewardPoints;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public Integer getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(Integer rewardPoints) {
        this.rewardPoints = rewardPoints;
    }
}
