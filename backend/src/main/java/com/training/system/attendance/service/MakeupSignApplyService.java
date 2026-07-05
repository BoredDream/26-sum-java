package com.training.system.attendance.service;

import com.training.system.attendance.dto.CurrentUserDTO;
import com.training.system.attendance.dto.MakeupApplyDTO;
import com.training.system.attendance.dto.MakeupApplyQueryDTO;
import com.training.system.attendance.dto.MakeupReviewDTO;
import com.training.system.attendance.vo.MakeupApplyVO;
import com.training.system.common.PageResult;

/**
 * 补签申请 Service
 */
public interface MakeupSignApplyService {

    Long applyMakeup(MakeupApplyDTO dto, CurrentUserDTO user);

    void reviewMakeup(Long applyId, MakeupReviewDTO dto, CurrentUserDTO user);

    PageResult<MakeupApplyVO> page(MakeupApplyQueryDTO dto, CurrentUserDTO user);
}
