package com.petschool.mapper;

import com.petschool.entity.Pet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PetMapper {
    int insert(Pet pet);
    Pet selectById(@Param("id") Long id);
    List<Pet> selectByUserId(@Param("userId") Long userId);
    int countByUserId(@Param("userId") Long userId);
    int update(Pet pet);
    int deleteById(@Param("id") Long id);
    List<Pet> selectPage(@Param("keyword") String keyword,
                         @Param("petType") String petType,
                         @Param("status") Integer status,
                         @Param("offset") int offset,
                         @Param("limit") int limit);
    int selectPageCount(@Param("keyword") String keyword,
                        @Param("petType") String petType,
                        @Param("status") Integer status);
    Pet selectDetailById(@Param("id") Long id);
    List<Map<String, Object>> selectStats();
    int countByPetType(@Param("petType") String petType);
    int countTodayNew();
}
