package com.training.system.info.mapper;

import com.training.system.info.entity.DataBackup;
import com.training.system.info.vo.BackupVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DataBackupMapper {

    int insert(DataBackup backup);

    int deleteById(@Param("backupId") Long backupId);

    DataBackup selectById(@Param("backupId") Long backupId);

    List<BackupVO> selectPage(@Param("offset") int offset, @Param("size") int size);

    long countPage();

    List<DataBackup> selectByTimeBefore(@Param("cutoff") LocalDateTime cutoff);
}
