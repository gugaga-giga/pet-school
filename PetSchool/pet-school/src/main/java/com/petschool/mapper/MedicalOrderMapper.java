package com.petschool.mapper;

import com.petschool.entity.MedicalOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MedicalOrderMapper {
    int insert(MedicalOrder order);
    MedicalOrder selectById(@Param("id") Long id);
    List<MedicalOrder> selectByUserId(@Param("userId") Long userId);
    List<MedicalOrder> selectPage(@Param("keyword") String keyword, @Param("status") Integer status, @Param("offset") int offset, @Param("pageSize") int pageSize);
    int selectPageCount(@Param("keyword") String keyword, @Param("status") Integer status);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int update(MedicalOrder order);
}
