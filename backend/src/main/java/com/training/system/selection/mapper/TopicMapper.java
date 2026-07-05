package com.training.system.selection.mapper;

import com.training.system.selection.entity.TopicEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TopicMapper {
    @Select("<script>" +
            "SELECT * FROM topic WHERE status = 'OPEN' " +
            "<if test='keyword != null and keyword != \"\"'>AND title LIKE CONCAT('%', #{keyword}, '%')</if> " +
            "ORDER BY create_time DESC" +
            "</script>")
    List<TopicEntity> findOpenTopics(@Param("keyword") String keyword);

    @Select("SELECT * FROM topic WHERE id = #{id}")
    TopicEntity findById(@Param("id") Long id);

    @Update("UPDATE topic SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
