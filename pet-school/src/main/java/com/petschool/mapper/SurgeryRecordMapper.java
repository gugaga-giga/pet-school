package com.petschool.mapper;

import com.petschool.entity.SurgeryRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SurgeryRecordMapper {
    int insert(SurgeryRecord record);
    List<SurgeryRecord> selectByPetId(@Param("petId") Long petId);
}
