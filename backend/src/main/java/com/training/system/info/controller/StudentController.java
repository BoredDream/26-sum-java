package com.training.system.info.controller;

import com.training.system.common.PageResult;
import com.training.system.common.Result;
import com.training.system.info.dto.StudentCreateDTO;
import com.training.system.info.dto.StudentUpdateDTO;
import com.training.system.info.entity.Student;
import com.training.system.info.service.StudentService;
import com.training.system.info.util.ExcelUtil;
import com.training.system.info.vo.StudentVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
    public Result<StudentVO> update(@PathVariable Long studentId, @Valid @RequestBody StudentUpdateDTO dto) {
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

    @DeleteMapping("/{studentId}")
    public Result<Void> delete(@PathVariable Long studentId) {
        studentService.deleteStudent(studentId);
        return Result.success();
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        try {
            List<Student> list = studentService.getAllStudents();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String encoded = URLEncoder.encode("学生信息.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
            ExcelUtil.writeXlsx(list, Student.class, response.getOutputStream(), "学生信息");
        } catch (Exception e) {
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    @PostMapping("/import")
    public Result<String> importStudents(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) return Result.fail(com.training.system.common.ResultCode.BAD_REQUEST, "请选择文件");
            List<Student> students = ExcelUtil.readXlsx(file, Student.class);
            if (students.isEmpty()) return Result.fail(com.training.system.common.ResultCode.BAD_REQUEST, "文件中没有有效数据");
            studentService.importStudents(students);
            return Result.success("成功导入 " + students.size() + " 条学生数据");
        } catch (Exception e) {
            return Result.fail(com.training.system.common.ResultCode.ERROR, "导入失败: " + e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestParam String oldPwd, @RequestParam String newPwd,
                                        @RequestParam Long studentId) {
        studentService.updateSelfPassword(studentId, oldPwd, newPwd);
        return Result.success();
    }
}
