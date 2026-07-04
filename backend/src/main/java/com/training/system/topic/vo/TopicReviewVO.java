package com.training.system.topic.vo;

import java.time.LocalDateTime;

/**
 * 审核记录视图对象
 */
public class TopicReviewVO {

    private Long reviewId;
    private Long topicId;
    private String adminName;
    private Integer reviewResult;
    private String reviewResultText;
    private String reviewComment;
    private LocalDateTime reviewTime;

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public Integer getReviewResult() {
        return reviewResult;
    }

    public void setReviewResult(Integer reviewResult) {
        this.reviewResult = reviewResult;
    }

    public String getReviewResultText() {
        return reviewResultText;
    }

    public void setReviewResultText(String reviewResultText) {
        this.reviewResultText = reviewResultText;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public LocalDateTime getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }
}
