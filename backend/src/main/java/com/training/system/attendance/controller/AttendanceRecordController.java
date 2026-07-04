package com.training.system.attendance.controller;

import com.training.system.attendance.dto.AttendanceRecordQueryDTO;
import com.training.system.attendance.dto.AttendanceSignDTO;
import com.training.system.attendance.dto.CurrentUserDTO;
import com.training.system.attendance.service.AttendanceRecordService;
import com.training.system.attendance.utils.SessionUtil;
import com.training.system.attendance.vo.AttendanceRecordVO;
import com.training.system.common.PageResult;
import com.training.system.common.Result;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 考勤记录 Controller
 */
@RestController
@RequestMapping("/api/attendance/record")
public class AttendanceRecordController {

    private final AttendanceRecordService attendanceRecordService;

    public AttendanceRecordController(AttendanceRecordService attendanceRecordService) {
        this.attendanceRecordService = attendanceRecordService;
    }

    @PostMapping("/sign")
    public Result<AttendanceRecordVO> sign(@Valid @RequestBody AttendanceSignDTO dto, HttpSession session) {
        CurrentUserDTO user = SessionUtil.getCurrentUser(session);
        AttendanceRecordVO vo = attendanceRecordService.sign(dto, user);
        return Result.success(vo);
    }

    @GetMapping("/page")
    public Result<PageResult<AttendanceRecordVO>> page(AttendanceRecordQueryDTO dto, HttpSession session) {
        CurrentUserDTO user = SessionUtil.getCurrentUser(session);
        PageResult<AttendanceRecordVO> pageResult = attendanceRecordService.page(dto, user);
        return Result.success(pageResult);
    }

    @GetMapping("/export")
    public void export(AttendanceRecordQueryDTO dto, HttpSession session, HttpServletResponse response) throws IOException {
        CurrentUserDTO user = SessionUtil.getCurrentUser(session);
        List<AttendanceRecordVO> list = attendanceRecordService.listForExport(dto, user);

        String fileName = URLEncoder.encode("考勤记录", StandardCharsets.UTF_8).replace("+", "%20") + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        try (Workbook workbook = new XSSFWorkbook();
             OutputStream out = response.getOutputStream()) {
            Sheet sheet = workbook.createSheet("考勤记录");
            String[] headers = {"任务标题", "学生姓名", "学号", "班级", "签到时间", "签到状态", "是否补签", "备注"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (AttendanceRecordVO vo : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(vo.getTaskTitle());
                row.createCell(1).setCellValue(vo.getStudentName());
                row.createCell(2).setCellValue(vo.getStudentNo());
                row.createCell(3).setCellValue(vo.getClassName());
                row.createCell(4).setCellValue(vo.getSignTime() != null ? vo.getSignTime().toString() : "");
                row.createCell(5).setCellValue(vo.getSignStatusName());
                row.createCell(6).setCellValue(vo.getIsMakeup() != null && vo.getIsMakeup() == 1 ? "是" : "否");
                row.createCell(7).setCellValue(vo.getRemark());
            }

            workbook.write(out);
        }
    }
}
