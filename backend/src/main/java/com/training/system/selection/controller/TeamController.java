package com.training.system.selection.controller;

import com.training.system.selection.dto.*;
import com.training.system.selection.service.TeamService;
import com.training.system.selection.vo.ApiResponse;
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
    public ApiResponse<TeamVO> createTeam(@RequestHeader("X-User-Id") Long userId,
                                          @RequestHeader("X-Role") String role,
                                          @RequestBody @Valid CreateTeamDTO dto) {
        return ApiResponse.success("团队创建成功", teamService.createTeam(userId, role, dto));
    }

    @GetMapping("/my")
    public ApiResponse<TeamVO> getMyTeam(@RequestHeader("X-User-Id") Long userId,
                                         @RequestHeader("X-Role") String role) {
        return ApiResponse.success(teamService.getMyTeam(userId, role));
    }

    @GetMapping("/{teamId}")
    public ApiResponse<TeamVO> getTeamDetail(@PathVariable Long teamId) {
        return ApiResponse.success(teamService.getTeamDetail(teamId));
    }

    @PostMapping("/{teamId}/join-requests")
    public ApiResponse<JoinRequestVO> applyJoin(@RequestHeader("X-User-Id") Long userId,
                                                 @RequestHeader("X-Role") String role,
                                                 @PathVariable Long teamId,
                                                 @RequestBody @Valid JoinTeamDTO dto) {
        return ApiResponse.success("入队申请已提交", teamService.applyJoin(userId, role, teamId, dto));
    }

    @GetMapping("/{teamId}/join-requests")
    public ApiResponse<List<JoinRequestVO>> getPendingJoinRequests(@RequestHeader("X-User-Id") Long userId,
                                                                    @RequestHeader("X-Role") String role,
                                                                    @PathVariable Long teamId) {
        return ApiResponse.success(teamService.listPendingJoinRequests(userId, role, teamId));
    }

    @PatchMapping("/join-requests/{requestId}/audit")
    public ApiResponse<JoinRequestVO> auditJoinRequest(@RequestHeader("X-User-Id") Long userId,
                                                        @RequestHeader("X-Role") String role,
                                                        @PathVariable Long requestId,
                                                        @RequestBody @Valid AuditJoinRequestDTO dto) {
        return ApiResponse.success("入队申请已处理", teamService.auditJoinRequest(userId, role, requestId, dto));
    }

    @PutMapping("/{teamId}/members/{studentId}/work-content")
    public ApiResponse<Void> updateMemberWork(@RequestHeader("X-User-Id") Long userId,
                                              @RequestHeader("X-Role") String role,
                                              @PathVariable Long teamId,
                                              @PathVariable Long studentId,
                                              @RequestBody @Valid UpdateMemberWorkDTO dto) {
        teamService.updateMemberWork(userId, role, teamId, studentId, dto);
        return ApiResponse.success("成员分工更新成功", null);
    }
}
