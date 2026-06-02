package com.petschool.mapper;

import com.petschool.entity.HealthRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HealthRecordMapper {
    int insert(HealthRecord record);
    List<HealthRecord> selectByPetId(@Param("petId") Long petId);
    HealthRecord selectLatestByPetId(@Param("petId") Long petId);
}
