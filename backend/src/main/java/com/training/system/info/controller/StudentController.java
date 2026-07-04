package com.training.system.info.controller;

import com.training.system.common.PageResult;
import com.training.system.common.Result;
import com.training.system.info.dto.StudentCreateDTO;
import com.training.system.info.service.StudentService;
import com.training.system.info.vo.StudentVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping
    public Result<StudentVO> create(@Valid @RequestBody StudentCreateDTO dto) {
        return Result.success(studentService.createStudent(dto));
    }

    @PutMapping("/{studentId}")
    public Result<StudentVO> update(@PathVariable Long studentId, @RequestBody StudentCreateDTO dto) {
        return Result.success(studentService.updateStudent(studentId, dto));
    }

    @GetMapping("/{studentId}")
    public Result<StudentVO> detail(@PathVariable Long studentId) {
        return Result.success(studentService.getStudentDetail(studentId));
    }

    @GetMapping("/page")
    public Result<PageResult<StudentVO>> page(@RequestParam(defaultValue = "") String keyword,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "15") int pageSize) {
        return Result.success(studentService.pageStudents(keyword, status, pageNum, pageSize));
    }

    @PostMapping("/{studentId}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long studentId) {
        studentService.resetPassword(studentId);
        return Result.success();
    }

    @PostMapping("/{studentId}/toggle-status")
    public Result<Void> toggleStatus(@PathVariable Long studentId) {
        studentService.toggleStatus(studentId);
        return Result.success();
    }
}
