package com.training.system.topic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 审核题目请求参数
 */
public class TopicReviewDTO {

    @NotNull(message = "请选择审核结果")
    private Integer reviewResult;

    @NotBlank(message = "请填写审核意见")
    private String reviewComment;

    public Integer getReviewResult() {
        return reviewResult;
    }

    public void setReviewResult(Integer reviewResult) {
        this.reviewResult = reviewResult;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }
}
