package com.training.system.score.mapper;

import com.training.system.score.entity.Contribution;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContributionMapper {
    int insert(Contribution contribution);
}

