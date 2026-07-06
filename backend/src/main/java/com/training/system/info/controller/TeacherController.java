package com.training.system.info.controller;

import com.training.system.common.PageResult;
import com.training.system.common.Result;
import com.training.system.info.annotation.OperationLog;
import com.training.system.info.dto.TeacherCreateDTO;
import com.training.system.info.dto.TeacherUpdateDTO;
import com.training.system.info.entity.Teacher;
import com.training.system.info.service.TeacherService;
import com.training.system.info.util.ExcelUtil;
import com.training.system.info.vo.TeacherVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @OperationLog(type = "CREATE", description = "新增教师")
    @PostMapping
    public Result<TeacherVO> create(@Valid @RequestBody TeacherCreateDTO dto) {
        return Result.success(teacherService.createTeacher(dto));
    }

    @OperationLog(type = "UPDATE", description = "修改教师信息")
    @PutMapping("/{teacherId}")
    public Result<TeacherVO> update(@PathVariable Long teacherId, @Valid @RequestBody TeacherUpdateDTO dto) {
        return Result.success(teacherService.updateTeacher(teacherId, dto));
    }

    @GetMapping("/{teacherId}")
    public Result<TeacherVO> detail(@PathVariable Long teacherId) {
        return Result.success(teacherService.getTeacherDetail(teacherId));
    }

    @GetMapping("/page")
    public Result<PageResult<TeacherVO>> page(@RequestParam(defaultValue = "") String keyword,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "15") int pageSize) {
        return Result.success(teacherService.pageTeachers(keyword, status, pageNum, pageSize));
    }

    @PostMapping("/{teacherId}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long teacherId) {
        teacherService.resetPassword(teacherId);
        return Result.success();
    }

    @PostMapping("/{teacherId}/toggle-role")
    public Result<Void> toggleRole(@PathVariable Long teacherId) {
        teacherService.toggleRole(teacherId);
        return Result.success();
    }

    @OperationLog(type = "DELETE", description = "删除教师")
    @DeleteMapping("/{teacherId}")
    public Result<Void> delete(@PathVariable Long teacherId) {
        teacherService.deleteTeacher(teacherId);
        return Result.success();
    }

    @OperationLog(type = "EXPORT", description = "导出教师信息")
    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        try {
            List<Teacher> list = teacherService.getAllTeachers();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String encoded = URLEncoder.encode("教师信息.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
            ExcelUtil.writeXlsx(list, Teacher.class, response.getOutputStream(), "教师信息");
        } catch (Exception e) {
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestParam String oldPwd, @RequestParam String newPwd,
                                        @RequestParam Long teacherId) {
        teacherService.updateSelfPassword(teacherId, oldPwd, newPwd);
        return Result.success();
    }
}
