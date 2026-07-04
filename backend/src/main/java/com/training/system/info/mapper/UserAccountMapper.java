package com.training.system.info.mapper;

import com.training.system.info.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAccountMapper {

    int insert(UserAccount userAccount);

    UserAccount selectByUsername(@Param("username") String username);

    UserAccount selectByRelatedIdAndRole(@Param("relatedId") Long relatedId, @Param("role") String role);

    int updatePassword(@Param("userId") Long userId, @Param("password") String password);

    int updateStatus(@Param("userId") Long userId, @Param("status") Integer status);
}
