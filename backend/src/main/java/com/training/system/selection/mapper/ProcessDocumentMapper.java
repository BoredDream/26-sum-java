package com.training.system.selection.mapper;

import com.training.system.selection.entity.ProcessDocumentEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProcessDocumentMapper {
    @Select("SELECT COUNT(1) FROM process_document WHERE team_id = #{teamId} AND document_type = #{documentType} AND project_stage = #{projectStage}")
    int countSameTypeAndStage(@Param("teamId") Long teamId,
                              @Param("documentType") String documentType,
                              @Param("projectStage") String projectStage);

    @Insert("INSERT INTO process_document(team_id, topic_id, document_name, document_type, project_stage, version_no, original_filename, stored_path, uploader_id, status, upload_time) " +
            "VALUES(#{teamId}, #{topicId}, #{documentName}, #{documentType}, #{projectStage}, #{versionNo}, #{originalFilename}, #{storedPath}, #{uploaderId}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProcessDocumentEntity document);

    @Select("SELECT * FROM process_document WHERE id = #{id}")
    ProcessDocumentEntity findById(@Param("id") Long id);

    @Select("SELECT * FROM process_document WHERE team_id = #{teamId} ORDER BY upload_time DESC")
    List<ProcessDocumentEntity> findByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT d.* FROM process_document d INNER JOIN topic tp ON tp.id = d.topic_id " +
            "WHERE tp.teacher_id = #{teacherId} ORDER BY d.upload_time DESC")
    List<ProcessDocumentEntity> findByTeacherId(@Param("teacherId") Long teacherId);

    @Select("SELECT * FROM process_document ORDER BY upload_time DESC")
    List<ProcessDocumentEntity> findAll();

    @Update("UPDATE process_document SET status = #{status}, teacher_feedback = #{teacherFeedback}, feedback_teacher_id = #{feedbackTeacherId}, feedback_time = NOW() WHERE id = #{id}")
    int updateFeedback(ProcessDocumentEntity document);
}
