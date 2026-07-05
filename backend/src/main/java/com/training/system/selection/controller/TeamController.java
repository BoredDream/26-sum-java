package com.training.system.selection.controller;

import com.training.system.selection.dto.*;
import com.training.system.selection.service.TeamService;
import com.training.system.common.Result;
import com.training.system.selection.vo.JoinRequestVO;
import com.training.system.selection.vo.TeamVO;
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

    @PostMapping
    public Result<TeamVO> createTeam(@RequestHeader("X-User-Id") Long userId,
                                          @RequestHeader("X-Role") String role,
                                          @RequestBody @Valid CreateTeamDTO dto) {
        return Result.success(teamService.createTeam(userId, role, dto));
    }

    @GetMapping("/my")
    public Result<TeamVO> getMyTeam(@RequestHeader("X-User-Id") Long userId,
                                         @RequestHeader("X-Role") String role) {
        return Result.success(teamService.getMyTeam(userId, role));
    }

    @GetMapping("/{teamId}")
    public Result<TeamVO> getTeamDetail(@PathVariable Long teamId) {
        return Result.success(teamService.getTeamDetail(teamId));
    }

    @PostMapping("/{teamId}/join-requests")
    public Result<JoinRequestVO> applyJoin(@RequestHeader("X-User-Id") Long userId,
                                                 @RequestHeader("X-Role") String role,
                                                 @PathVariable Long teamId,
                                                 @RequestBody @Valid JoinTeamDTO dto) {
        return Result.success(teamService.applyJoin(userId, role, teamId, dto));
    }

    @GetMapping("/{teamId}/join-requests")
    public Result<List<JoinRequestVO>> getPendingJoinRequests(@RequestHeader("X-User-Id") Long userId,
                                                                    @RequestHeader("X-Role") String role,
                                                                    @PathVariable Long teamId) {
        return Result.success(teamService.listPendingJoinRequests(userId, role, teamId));
    }

    @PatchMapping("/join-requests/{requestId}/audit")
    public Result<JoinRequestVO> auditJoinRequest(@RequestHeader("X-User-Id") Long userId,
                                                        @RequestHeader("X-Role") String role,
                                                        @PathVariable Long requestId,
                                                        @RequestBody @Valid AuditJoinRequestDTO dto) {
        return Result.success(teamService.auditJoinRequest(userId, role, requestId, dto));
    }

    @PutMapping("/{teamId}/members/{studentId}/work-content")
    public Result<Void> updateMemberWork(@RequestHeader("X-User-Id") Long userId,
                                              @RequestHeader("X-Role") String role,
                                              @PathVariable Long teamId,
                                              @PathVariable Long studentId,
                                              @RequestBody @Valid UpdateMemberWorkDTO dto) {
        teamService.updateMemberWork(userId, role, teamId, studentId, dto);
        return Result.success();
    }
}
