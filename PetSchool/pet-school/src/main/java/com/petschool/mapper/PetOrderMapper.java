package com.petschool.mapper;

import com.petschool.entity.PetOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PetOrderMapper {
    int insert(PetOrder order);
    PetOrder selectById(@Param("id") Long id);
    List<PetOrder> selectByUserId(@Param("userId") Long userId);
    List<PetOrder> selectPage(@Param("keyword") String keyword, @Param("status") Integer status, @Param("offset") int offset, @Param("pageSize") int pageSize);
    int selectPageCount(@Param("keyword") String keyword, @Param("status") Integer status);
    List<PetOrder> selectAll();
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int deleteById(@Param("id") Long id);
    int deleteBatch(@Param("ids") List<Long> ids);
}
