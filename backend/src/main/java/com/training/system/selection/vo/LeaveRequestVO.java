package com.training.system.selection.vo;

import java.time.LocalDateTime;

public class LeaveRequestVO {
    private Long id;
    private Long teamId;
    private Long applicantId;
    private String leaveMessage;
    private String status;
    private Long reviewerId;
    private String reviewOpinion;
    private LocalDateTime applyTime;
    private LocalDateTime reviewTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public Long getApplicantId() { return applicantId; }
    public void setApplicantId(Long applicantId) { this.applicantId = applicantId; }
    public String getLeaveMessage() { return leaveMessage; }
    public void setLeaveMessage(String leaveMessage) { this.leaveMessage = leaveMessage; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }
    public String getReviewOpinion() { return reviewOpinion; }
    public void setReviewOpinion(String reviewOpinion) { this.reviewOpinion = reviewOpinion; }
    public LocalDateTime getApplyTime() { return applyTime; }
    public void setApplyTime(LocalDateTime applyTime) { this.applyTime = applyTime; }
    public LocalDateTime getReviewTime() { return reviewTime; }
    public void setReviewTime(LocalDateTime reviewTime) { this.reviewTime = reviewTime; }
}
