package com.petschool.mapper;

import com.petschool.entity.DewormingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DewormingRecordMapper {
    int insert(DewormingRecord record);
    DewormingRecord selectById(@Param("id") Long id);
    List<DewormingRecord> selectByPetId(@Param("petId") Long petId);
    List<DewormingRecord> selectUpcoming(@Param("userId") Long userId, @Param("days") int days);
    List<DewormingRecord> selectExpired(@Param("userId") Long userId);
    List<DewormingRecord> selectAll();
    int update(DewormingRecord record);
    int deleteById(@Param("id") Long id);
}
