package com.petschool.mapper;

import com.petschool.entity.TrainingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TrainingRecordMapper {
    int insert(TrainingRecord record);
    List<TrainingRecord> selectByPetId(@Param("petId") Long petId);
}
