package com.training.system.selection.controller;

import com.training.system.info.annotation.OperationLog;
import com.training.system.selection.dto.*;
import com.training.system.selection.service.TeamService;
import com.training.system.common.Result;
import com.training.system.selection.util.SelectionSessionUtil;
import com.training.system.selection.util.SelectionSessionUtil.CurrentUser;
import com.training.system.selection.vo.JoinRequestVO;
import com.training.system.selection.vo.LeaveRequestVO;
import com.training.system.selection.vo.TeamVO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/selection/teams")
public class TeamController {
    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @OperationLog(type = "CREATE", description = "创建团队")
    @PostMapping
    public Result<TeamVO> createTeam(HttpSession session,
                                     @RequestBody @Valid CreateTeamDTO dto) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        return Result.success(teamService.createTeam(user.relatedId(), user.role(), dto));
    }

    @GetMapping("/my")
    public Result<List<TeamVO>> getMyTeams(HttpSession session) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        return Result.success(teamService.getMyTeams(user.relatedId(), user.role()));
    }

    @GetMapping
    public Result<List<TeamVO>> listJoinableTeams() {
        return Result.success(teamService.listJoinableTeams());
    }

    @GetMapping("/{teamId}")
    public Result<TeamVO> getTeamDetail(@PathVariable Long teamId) {
        return Result.success(teamService.getTeamDetail(teamId));
    }

    @OperationLog(type = "CREATE", description = "申请加入团队")
    @PostMapping("/{teamId}/join-requests")
    public Result<JoinRequestVO> applyJoin(HttpSession session,
                                           @PathVariable Long teamId,
                                           @RequestBody @Valid JoinTeamDTO dto) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        return Result.success(teamService.applyJoin(user.relatedId(), user.role(), teamId, dto));
    }

    @GetMapping("/{teamId}/join-requests")
    public Result<List<JoinRequestVO>> getPendingJoinRequests(HttpSession session,
                                                              @PathVariable Long teamId) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        return Result.success(teamService.listPendingJoinRequests(user.relatedId(), user.role(), teamId));
    }

    @OperationLog(type = "UPDATE", description = "审核入队申请")
    @PatchMapping("/join-requests/{requestId}/audit")
    public Result<JoinRequestVO> auditJoinRequest(HttpSession session,
                                                  @PathVariable Long requestId,
                                                  @RequestBody @Valid AuditJoinRequestDTO dto) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        return Result.success(teamService.auditJoinRequest(user.relatedId(), user.role(), requestId, dto));
    }

    @OperationLog(type = "CREATE", description = "申请离开团队")
    @PostMapping("/{teamId}/leave-requests")
    public Result<LeaveRequestVO> requestLeave(HttpSession session,
                                               @PathVariable Long teamId,
                                               @RequestBody @Valid RequestLeaveTeamDTO dto) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        return Result.success(teamService.requestLeave(user.relatedId(), user.role(), teamId, dto));
    }

    @GetMapping("/{teamId}/leave-requests")
    public Result<List<LeaveRequestVO>> getPendingLeaveRequests(HttpSession session,
                                                                 @PathVariable Long teamId) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        return Result.success(teamService.listPendingLeaveRequests(user.relatedId(), user.role(), teamId));
    }

    @OperationLog(type = "UPDATE", description = "审核离队申请")
    @PatchMapping("/leave-requests/{requestId}/audit")
    public Result<LeaveRequestVO> auditLeaveRequest(HttpSession session,
                                                     @PathVariable Long requestId,
                                                     @RequestBody @Valid AuditLeaveRequestDTO dto) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        return Result.success(teamService.auditLeaveRequest(user.relatedId(), user.role(), requestId, dto));
    }

    @OperationLog(type = "UPDATE", description = "修改成员工作内容")
    @PutMapping("/{teamId}/members/{studentId}/work-content")
    public Result<Void> updateMemberWork(HttpSession session,
                                         @PathVariable Long teamId,
                                         @PathVariable Long studentId,
                                         @RequestBody @Valid UpdateMemberWorkDTO dto) {
        CurrentUser user = SelectionSessionUtil.currentUser(session);
        teamService.updateMemberWork(user.relatedId(), user.role(), teamId, studentId, dto);
        return Result.success();
    }
}
