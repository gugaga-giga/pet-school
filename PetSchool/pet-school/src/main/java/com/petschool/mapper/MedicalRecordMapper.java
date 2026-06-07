package com.petschool.mapper;

import com.petschool.entity.MedicalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MedicalRecordMapper {
    int insert(MedicalRecord record);
    MedicalRecord selectById(@Param("id") Long id);
    MedicalRecord selectByMedicalOrderId(@Param("medicalOrderId") Long medicalOrderId);
    List<MedicalRecord> selectByPetId(@Param("petId") Long petId);
    List<MedicalRecord> selectPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize);
    int selectPageCount(@Param("keyword") String keyword);
    int update(MedicalRecord record);
    int deleteById(@Param("id") Long id);
}
