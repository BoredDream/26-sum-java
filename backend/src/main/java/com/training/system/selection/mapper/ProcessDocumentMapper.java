package com.training.system.selection.mapper;

import com.training.system.selection.entity.ProcessDocumentEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProcessDocumentMapper {
    @Select("SELECT COUNT(1) FROM process_document WHERE team_id = #{teamId} AND document_type = #{documentType} AND project_stage = #{projectStage} AND is_deleted = 0")
    int countSameTypeAndStage(@Param("teamId") Long teamId,
                              @Param("documentType") String documentType,
                              @Param("projectStage") String projectStage);

    @Insert("INSERT INTO process_document(team_id, topic_id, document_name, document_type, project_stage, file_path, file_size, version_no, uploader_id, audit_status, stage_id, remark, upload_time, create_time) " +
            "VALUES(#{teamId}, #{topicId}, #{documentName}, #{documentType}, #{projectStage}, #{storedPath}, #{fileSize}, #{versionNo}, #{uploaderId}, CASE WHEN #{status} = 'RETURNED' THEN 2 WHEN #{status} = 'REVIEWED' THEN 1 ELSE 0 END, #{stageId}, #{remark}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "document_id")
    int insert(ProcessDocumentEntity document);

    @Select("SELECT document_id AS id, team_id, topic_id, document_name, document_type, project_stage, version_no, " +
            "file_path AS original_filename, file_path AS stored_path, file_size, uploader_id, " +
            "CASE audit_status WHEN 1 THEN 'REVIEWED' WHEN 2 THEN 'RETURNED' ELSE 'SUBMITTED' END AS status, " +
            "teacher_feedback, feedback_teacher_id, stage_id, remark, upload_time, update_time AS feedback_time " +
            "FROM process_document WHERE document_id = #{id} AND is_deleted = 0")
    ProcessDocumentEntity findById(@Param("id") Long id);

    @Select("SELECT document_id AS id, team_id, topic_id, document_name, document_type, project_stage, version_no, " +
            "file_path AS original_filename, file_path AS stored_path, file_size, uploader_id, " +
            "CASE audit_status WHEN 1 THEN 'REVIEWED' WHEN 2 THEN 'RETURNED' ELSE 'SUBMITTED' END AS status, " +
            "teacher_feedback, feedback_teacher_id, stage_id, remark, upload_time, update_time AS feedback_time " +
            "FROM process_document WHERE team_id = #{teamId} AND is_deleted = 0 ORDER BY upload_time DESC")
    List<ProcessDocumentEntity> findByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT d.document_id AS id, d.team_id, d.topic_id, d.document_name, d.document_type, d.project_stage, d.version_no, " +
            "d.file_path AS original_filename, d.file_path AS stored_path, d.file_size, d.uploader_id, " +
            "CASE d.audit_status WHEN 1 THEN 'REVIEWED' WHEN 2 THEN 'RETURNED' ELSE 'SUBMITTED' END AS status, " +
            "d.teacher_feedback, d.feedback_teacher_id, d.stage_id, d.remark, d.upload_time, d.update_time AS feedback_time " +
            "FROM process_document d INNER JOIN project_topic tp ON tp.topic_id = d.topic_id " +
            "WHERE tp.teacher_id = #{teacherId} AND d.is_deleted = 0 ORDER BY d.upload_time DESC")
    List<ProcessDocumentEntity> findByTeacherId(@Param("teacherId") Long teacherId);

    @Select("SELECT document_id AS id, team_id, topic_id, document_name, document_type, project_stage, version_no, " +
            "file_path AS original_filename, file_path AS stored_path, file_size, uploader_id, " +
            "CASE audit_status WHEN 1 THEN 'REVIEWED' WHEN 2 THEN 'RETURNED' ELSE 'SUBMITTED' END AS status, " +
            "teacher_feedback, feedback_teacher_id, stage_id, remark, upload_time, update_time AS feedback_time " +
            "FROM process_document WHERE is_deleted = 0 ORDER BY upload_time DESC")
    List<ProcessDocumentEntity> findAll();

    @Update("UPDATE process_document SET audit_status = CASE WHEN #{status} = 'RETURNED' THEN 2 WHEN #{status} = 'REVIEWED' THEN 1 ELSE 0 END, " +
            "teacher_feedback = #{teacherFeedback}, feedback_teacher_id = #{feedbackTeacherId}, update_time = NOW() WHERE document_id = #{id}")
    int updateFeedback(ProcessDocumentEntity document);
}
