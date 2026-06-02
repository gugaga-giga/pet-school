package com.petschool.mapper;

import com.petschool.entity.Pet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PetMapper {
    int insert(Pet pet);
    Pet selectById(@Param("id") Long id);
    List<Pet> selectByUserId(@Param("userId") Long userId);
}
