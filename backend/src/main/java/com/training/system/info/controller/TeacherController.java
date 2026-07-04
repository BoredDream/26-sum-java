package com.training.system.info.controller;

import com.training.system.common.PageResult;
import com.training.system.common.Result;
import com.training.system.info.dto.TeacherCreateDTO;
import com.training.system.info.service.TeacherService;
import com.training.system.info.vo.TeacherVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @PostMapping
    public Result<TeacherVO> create(@RequestBody TeacherCreateDTO dto) {
        return Result.success(teacherService.createTeacher(dto));
    }

    @PutMapping("/{teacherId}")
    public Result<TeacherVO> update(@PathVariable Long teacherId, @RequestBody TeacherCreateDTO dto) {
        return Result.success(teacherService.updateTeacher(teacherId, dto));
    }

    @GetMapping("/{teacherId}")
    public Result<TeacherVO> detail(@PathVariable Long teacherId) {
        return Result.success(teacherService.getTeacherDetail(teacherId));
    }

    @GetMapping("/page")
    public Result<PageResult<TeacherVO>> page(@RequestParam(defaultValue = "") String keyword,
                                               @RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "15") int pageSize) {
        return Result.success(teacherService.pageTeachers(keyword, pageNum, pageSize));
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

    @DeleteMapping("/{teacherId}")
    public Result<Void> delete(@PathVariable Long teacherId) {
        teacherService.deleteTeacher(teacherId);
        return Result.success();
    }
}
