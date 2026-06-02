package com.petschool.mapper;

import com.petschool.entity.VaccineRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VaccineRecordMapper {
    int insert(VaccineRecord record);
    VaccineRecord selectById(@Param("id") Long id);
    List<VaccineRecord> selectByPetId(@Param("petId") Long petId);
    List<VaccineRecord> selectUpcoming(@Param("userId") Long userId, @Param("days") int days);
    List<VaccineRecord> selectExpired(@Param("userId") Long userId);
    List<VaccineRecord> selectAll();
    int update(VaccineRecord record);
    int deleteById(@Param("id") Long id);
}
