package com.petschool.mapper;

import com.petschool.entity.TrainingVideo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TrainingVideoMapper {
    int insert(TrainingVideo video);
    List<TrainingVideo> selectByRecordId(@Param("recordId") Long recordId);
}
