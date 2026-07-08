package com.training.system.attendance.mapper;

import com.training.system.attendance.entity.MakeupSignApply;
import com.training.system.attendance.vo.MakeupApplyVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 补签申请 Mapper
 */
@Mapper
public interface MakeupSignApplyMapper {

    @Insert("INSERT INTO makeup_sign_apply(task_id, record_id, student_id, apply_reason, " +
            "proof_file_path, audit_status, create_time, update_time) " +
            "VALUES(#{taskId}, #{recordId}, #{studentId}, #{applyReason}, " +
            "#{proofFilePath}, 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "applyId")
    int insert(MakeupSignApply apply);

    @Select("SELECT * FROM makeup_sign_apply WHERE apply_id = #{applyId}")
    MakeupSignApply selectById(Long applyId);

    @Select("SELECT * FROM makeup_sign_apply WHERE task_id = #{taskId} AND student_id = #{studentId} AND audit_status = 0")
    MakeupSignApply selectPendingByTaskAndStudent(@Param("taskId") Long taskId, @Param("studentId") Long studentId);

    @Update("UPDATE makeup_sign_apply SET audit_status = #{auditStatus}, audit_teacher_id = #{auditTeacherId}, " +
            "audit_comment = #{auditComment}, audit_time = NOW(), update_time = NOW() " +
            "WHERE apply_id = #{applyId}")
    int updateAudit(MakeupSignApply apply);

    long countByCondition(@Param("taskId") Long taskId,
                          @Param("auditStatus") Integer auditStatus,
                          @Param("studentId") Long studentId,
                          @Param("teacherId") Long teacherId);

    List<MakeupApplyVO> selectPage(@Param("taskId") Long taskId,
                                   @Param("auditStatus") Integer auditStatus,
                                   @Param("studentId") Long studentId,
                                   @Param("teacherId") Long teacherId,
                                   @Param("offset") int offset,
                                   @Param("limit") int limit);

    /**
     * 统计学生未查看的补签审核结果数
     */
    long countUnviewedResults(@Param("studentId") Long studentId);

    /**
     * 将学生所有已审核的补签申请标记为已查看
     */
    int markResultsViewed(@Param("studentId") Long studentId);
}
