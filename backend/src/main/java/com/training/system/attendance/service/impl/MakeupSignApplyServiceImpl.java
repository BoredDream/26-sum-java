package com.training.system.attendance.service.impl;

import com.training.system.attendance.dto.CurrentUserDTO;
import com.training.system.attendance.dto.MakeupApplyDTO;
import com.training.system.attendance.dto.MakeupApplyQueryDTO;
import com.training.system.attendance.dto.MakeupReviewDTO;
import com.training.system.attendance.entity.AttendanceRecord;
import com.training.system.attendance.entity.AttendanceTask;
import com.training.system.attendance.entity.MakeupSignApply;
import com.training.system.attendance.enums.AttendanceSignStatusEnum;
import com.training.system.attendance.enums.MakeupAuditStatusEnum;
import com.training.system.attendance.mapper.AttendanceRecordMapper;
import com.training.system.attendance.mapper.AttendanceTaskMapper;
import com.training.system.attendance.mapper.MakeupSignApplyMapper;
import com.training.system.attendance.service.MakeupSignApplyService;
import com.training.system.attendance.vo.MakeupApplyVO;
import com.training.system.common.PageResult;
import com.training.system.common.ResultCode;
import com.training.system.exception.BusinessException;
import com.training.system.info.mapper.TeacherMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 补签申请 Service 实现
 */
@Service
public class MakeupSignApplyServiceImpl implements MakeupSignApplyService {

    private static final String PROOF_PATH_PREFIX = "attendance/";
    private static final Set<String> ALLOWED_PROOF_EXTENSIONS = Set.of("pdf", "png", "jpg", "jpeg", "doc", "docx");

    private final MakeupSignApplyMapper makeupSignApplyMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final AttendanceTaskMapper attendanceTaskMapper;
    private final TeacherMapper teacherMapper;

    public MakeupSignApplyServiceImpl(MakeupSignApplyMapper makeupSignApplyMapper,
                                      AttendanceRecordMapper attendanceRecordMapper,
                                      AttendanceTaskMapper attendanceTaskMapper,
                                      TeacherMapper teacherMapper) {
        this.makeupSignApplyMapper = makeupSignApplyMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.attendanceTaskMapper = attendanceTaskMapper;
        this.teacherMapper = teacherMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long applyMakeup(MakeupApplyDTO dto, CurrentUserDTO user) {
        if (!user.isStudent()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅学生可提交补签申请");
        }

        Long studentId = user.getRelatedId();
        AttendanceRecord record = attendanceRecordMapper.selectByTaskAndStudent(dto.getTaskId(), studentId);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "考勤记录不存在");
        }

        if (record.getSignStatus() != null) {
            if (record.getSignStatus() == AttendanceSignStatusEnum.NORMAL.getCode()) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "当前考勤记录无需补签");
            }
            if (record.getSignStatus() == AttendanceSignStatusEnum.MAKEUP.getCode()) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "该考勤记录已通过补签，请勿重复申请");
            }
        }

        MakeupSignApply pending = makeupSignApplyMapper.selectPendingByTaskAndStudent(dto.getTaskId(), studentId);
        if (pending != null) {
            throw new BusinessException(ResultCode.CONFLICT, "该签到任务已有待审核补签申请");
        }
        validateProofFilePath(dto);

        MakeupSignApply apply = new MakeupSignApply();
        BeanUtils.copyProperties(dto, apply);
        apply.setRecordId(record.getRecordId());
        apply.setStudentId(studentId);
        apply.setAuditStatus(MakeupAuditStatusEnum.PENDING.getCode());
        makeupSignApplyMapper.insert(apply);

        return apply.getApplyId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewMakeup(Long applyId, MakeupReviewDTO dto, CurrentUserDTO user) {
        if (!user.isTeacher() && !user.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限审核补签申请");
        }

        MakeupSignApply apply = makeupSignApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "补签申请不存在");
        }
        if (apply.getAuditStatus() != null && apply.getAuditStatus() != MakeupAuditStatusEnum.PENDING.getCode()) {
            throw new BusinessException(ResultCode.CONFLICT, "该申请已审核，不能重复操作");
        }

        AttendanceTask task = attendanceTaskMapper.selectById(apply.getTaskId());
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "签到任务不存在");
        }

        if (user.isTeacher() && !task.getTeacherId().equals(user.getRelatedId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能审核本人发布任务的补签申请");
        }

        Integer auditStatus = dto.getAuditStatus();
        if (auditStatus == null || (auditStatus != MakeupAuditStatusEnum.APPROVED.getCode()
                && auditStatus != MakeupAuditStatusEnum.REJECTED.getCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "审核结果不正确");
        }
        if (auditStatus == MakeupAuditStatusEnum.REJECTED.getCode()
                && (dto.getAuditComment() == null || dto.getAuditComment().isBlank())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请填写审核意见");
        }

        apply.setAuditStatus(auditStatus);
        apply.setAuditTeacherId(user.getRelatedId());
        apply.setAuditComment(dto.getAuditComment());
        makeupSignApplyMapper.updateAudit(apply);

        if (auditStatus == MakeupAuditStatusEnum.APPROVED.getCode()) {
            AttendanceRecord record = attendanceRecordMapper.selectById(apply.getRecordId());
            if (record == null) {
                record = attendanceRecordMapper.selectByTaskAndStudent(apply.getTaskId(), apply.getStudentId());
            }
            if (record != null) {
                record.setSignStatus(AttendanceSignStatusEnum.MAKEUP.getCode());
                record.setIsMakeup(1);
                attendanceRecordMapper.update(record);
            }
        }
    }

    private void validateProofFilePath(MakeupApplyDTO dto) {
        String proofFilePath = dto.getProofFilePath();
        if (proofFilePath == null || proofFilePath.isBlank()) {
            dto.setProofFilePath(null);
            return;
        }
        String normalized = proofFilePath.trim().replace('\\', '/');
        if (normalized.startsWith("/") || normalized.contains(":") || normalized.contains("../")
                || normalized.contains("/..") || !normalized.startsWith(PROOF_PATH_PREFIX)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "证明材料路径不合法");
        }
        String fileName = normalized.substring(PROOF_PATH_PREFIX.length());
        if (fileName.isBlank() || fileName.contains("/")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "证明材料路径不合法");
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 1 || dotIndex == fileName.length() - 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "证明材料格式不支持");
        }
        String extension = fileName.substring(dotIndex + 1).toLowerCase();
        if (!ALLOWED_PROOF_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "证明材料格式不支持");
        }
        dto.setProofFilePath(normalized);
    }

    @Override
    public PageResult<MakeupApplyVO> page(MakeupApplyQueryDTO dto, CurrentUserDTO user) {
        Long teacherId = user.isTeacher() ? user.getRelatedId() : null;
        Long studentId = user.isStudent() ? user.getRelatedId() : null;

        int offset = (dto.getPageNum() - 1) * dto.getPageSize();
        long total = makeupSignApplyMapper.countByCondition(dto.getTaskId(), dto.getAuditStatus(), studentId, teacherId);
        List<MakeupApplyVO> list = makeupSignApplyMapper.selectPage(
                dto.getTaskId(), dto.getAuditStatus(), studentId, teacherId, offset, dto.getPageSize());

        fillMakeupVoNames(list);
        return new PageResult<>(list, total, dto.getPageNum(), dto.getPageSize());
    }

    @Override
    public long countUnviewedResults(Long studentId) {
        return makeupSignApplyMapper.countUnviewedResults(studentId);
    }

    @Override
    public String getProofPath(Long applyId) {
        MakeupSignApply apply = makeupSignApplyMapper.selectById(applyId);
        return apply != null ? apply.getProofFilePath() : null;
    }

    @Override
    @Transactional
    public void markResultsViewed(Long studentId) {
        makeupSignApplyMapper.markResultsViewed(studentId);
    }

    private void fillMakeupVoNames(List<MakeupApplyVO> list) {
        // 收集所有需要补填教师姓名的审核教师ID
        List<Long> teacherIds = list.stream()
                .filter(vo -> vo.getAuditTeacherId() != null && vo.getAuditTeacherName() == null)
                .map(MakeupApplyVO::getAuditTeacherId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> teacherNameMap = new HashMap<>();
        if (!teacherIds.isEmpty()) {
            List<Map<String, Object>> rows = teacherMapper.selectTeacherNamesByIds(teacherIds);
            for (Map<String, Object> row : rows) {
                Long id = (Long) row.get("teacher_id");
                String name = (String) row.get("teacher_name");
                if (id != null) {
                    teacherNameMap.put(id, name);
                }
            }
        }

        for (MakeupApplyVO vo : list) {
            vo.setAuditStatusName(MakeupAuditStatusEnum.getDescByCode(vo.getAuditStatus()));
            if (vo.getAuditTeacherId() != null && vo.getAuditTeacherName() == null) {
                vo.setAuditTeacherName(teacherNameMap.get(vo.getAuditTeacherId()));
            }
        }
    }
}
