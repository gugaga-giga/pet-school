package com.petschool.mapper;

import com.petschool.entity.PetHealthRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PetHealthRecordMapper {
    int insert(PetHealthRecord record);
    PetHealthRecord selectById(@Param("id") Long id);
    List<PetHealthRecord> selectByPetId(@Param("petId") Long petId);
    PetHealthRecord selectLatestByPetId(@Param("petId") Long petId);
    List<PetHealthRecord> selectPage(@Param("keyword") String keyword, @Param("riskLevel") Integer riskLevel, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("offset") int offset, @Param("pageSize") int pageSize);
    int selectPageCount(@Param("keyword") String keyword, @Param("riskLevel") Integer riskLevel, @Param("startDate") String startDate, @Param("endDate") String endDate);
    List<PetHealthRecord> selectAll();
    int update(PetHealthRecord record);
    int deleteById(@Param("id") Long id);
    List<PetHealthRecord> selectTrendByPetId(@Param("petId") Long petId, @Param("limit") int limit);
}
